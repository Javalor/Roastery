package io.javalor.roastery;

import io.javalor.roastery.annotation.AutoInjectd;
import io.javalor.roastery.annotation.AutoInstatiation;
import io.javalor.roastery.annotation.Qualifier;
import io.javalor.roastery.registry.BeanRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;


//TODO: Chain dependency
public class DependencyInjector<T> {

    private final Class<T> component;
    private final Set<Class<?>> beanClassRegistry;
    private final T instance;
    private static final Logger logger = LoggerFactory.getLogger(DependencyInjector.class);

    private DependencyInjector(Class<T> component, Set<Class<?>> beanClassRegistry) {
        this.component = component;
        this.instance = null;
        this.beanClassRegistry = beanClassRegistry;
    }

    @SuppressWarnings({"unchecked"})
    private DependencyInjector(T instance, Set<Class<?>> beanClassRegistry) {
        this.component = (Class<T>) instance.getClass();
        this.instance = instance;
        this.beanClassRegistry = beanClassRegistry;
    }

    public static <U> DependencyInjector<U> forClass(Class<U> component, Set<Class<?>> beanClassRegistry) {
        return new DependencyInjector<>(component, beanClassRegistry);
    }

    public DependencyInjector<T> forInstance(T instance) {
        return new DependencyInjector<>(instance, beanClassRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public T getClassInstance() throws Exception {

        logger.debug("Component "+ component.getCanonicalName());
        Set<Constructor<?>> constructorSet = Arrays.stream(component.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(AutoInjectd.class))
                .collect(Collectors.toSet());

        if (constructorSet.size()>1) {
            throw new Exception("Ambiguous (more than one AutoInjected Constructor found)");
        }

        T instance;
        if (constructorSet.isEmpty()) {

            instance = component.getConstructor().newInstance();
        }
        else {

            System.out.println("Found constructor");
            Constructor<?> constructor = constructorSet.iterator().next();
            boolean access = constructor.canAccess(null);
            constructor.setAccessible(true);
            instance = (T) constructor.newInstance(buildParameter(constructor.getParameterTypes(), constructor.getParameterAnnotations(), beanClassRegistry));
            constructor.setAccessible(access);
        }


        List<Exception> fieldInjectionException = this.injectAllFields();
//                Arrays.stream(component.getDeclaredFields())
//                .filter(DependencyInjector::isAutoInjectedAnnotated)
//                .peek(field->logger.debug("Field: "+field.getType().getSimpleName()+" "+field.getName()))
//                .map(field -> injectField(instance, field, beanClassRegistry))
//                .filter(Objects::nonNull).peek(e->logger.error("Field injection exception(s)", e))
//                .collect(Collectors.toUnmodifiableList());

        List<Exception> methodInjectionException = Arrays.stream(component.getDeclaredMethods())
                .filter(DependencyInjector::isAutoInjectedAnnotated)
                .peek(method->logger.debug("Method: "+method.getName()+"("+ Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "))+")"))
                .map(method -> injectMethod(instance, method, beanClassRegistry))
                .filter(Objects::nonNull).peek(e->logger.error("Method injection exception(s)", e))
                .collect(Collectors.toUnmodifiableList());


        return instance;
    }


    public List<Exception> injectAllFields(BeanRegistry beanRegistry) {
        Objects.requireNonNull(this.instance);
        return Arrays.stream(this.component.getDeclaredFields())
                .filter(DependencyInjector::isAutoInjectedAnnotated)
                .peek(field->logger.debug("Field: "+field.getType().getSimpleName()+" "+field.getName()))
                .map(field -> injectField(this.instance, field, this.beanClassRegistry))
                .filter(Objects::nonNull).peek(e->logger.error("Field injection exception(s)", e))
                .collect(Collectors.toUnmodifiableList());
    }

    public static Exception injectMethod(Object instance, Method method, Set<Class<?>> beanClassRegistry) {

        try {
            return forceInvoke(instance, method,
                    buildParameter(method.getParameterTypes(), method.getParameterAnnotations(), beanClassRegistry) );
        } catch (Exception e) {
            return e;
        }
    }

    public static Exception injectField(Object instance, Field field, Set<Class<?>> beanClassRegistry) {

        try {

            Class<?> assignableClass = lookupQualifier(beanClassRegistry, field);
            if (assignableClass == null) {
                throw new Exception("Assignable bean Not Found");
            }

            return forceSet(instance, field, assignableClass.getDeclaredConstructor().newInstance());

        } catch (Exception e) {
            return e;
        }



    }

    public static Boolean isAutoInjectedAnnotated(Constructor<?> constructor) {
        return constructor.isAnnotationPresent(AutoInjectd.class);
    }

    public static Boolean isAutoInjectedAnnotated(Method method) {
        return method.isAnnotationPresent(AutoInjectd.class);
    }

    public static Boolean isAutoInstantiationAnnotated(Method method) {
        return method.isAnnotationPresent(AutoInstatiation.class);
    }

    public static Boolean isAutoInjectedAnnotated(Field field) {
        return field.isAnnotationPresent(AutoInjectd.class);
    }

    public static Boolean isQualifierAnnotated(Field field) {
        return field.isAnnotationPresent(Qualifier.class);
    }

    public static Boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static Boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public static Exception forceInvoke(Object instance, Method method, Object[] args) {
        return forceInvoke(instance, method, args, null);
    }

    public static Exception forceInvoke(Object instance, Method method, Object[] args, Reference<Object> returnValue) {
        boolean access = method.canAccess(instance);

        try {
            method.setAccessible(true);
            Objects.requireNonNullElse(returnValue, new Reference<>()).set(method.invoke(instance, args));
            method.setAccessible(access);
        } catch (IllegalAccessException | InvocationTargetException e) {
            method.setAccessible(access);
            return e;
        }
        return null;
    }

    public static IllegalAccessException forceSet(Object instance, Field field, Object value)  {
        boolean access = field.canAccess(instance);

        try {
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(access);
        } catch (IllegalAccessException e) {
            field.setAccessible(access);
            return e;
        }

        return null;
    }

    protected static Object[] buildParameter(Class<?>[] parameters, Annotation[][] annotation, Set<Class<?>> beanClassRegistry) throws Exception  {
        List<Object> objectList = new LinkedList<>();

        for (int i = 0; i < parameters.length; i++) {
            objectList.add(buildObject(parameters[i],annotation[i], beanClassRegistry));
        }
        return objectList.toArray();
    }

    protected static Object buildObject(Class<?> parameter, Annotation[] annotations, Set<Class<?>> beanClassRegistry) throws Exception {

        Set<Annotation> annotation = Arrays.stream(annotations)
                .filter(a->Qualifier.class.equals(a.annotationType())).collect(Collectors.toSet());
        if (!annotation.isEmpty()) {
            Qualifier qualifier = (Qualifier) annotation.iterator().next();

            System.out.println(qualifier);
            System.out.println(parameter);
            if (parameter.isAssignableFrom(qualifier.value())) {
                return qualifier.value().getDeclaredConstructor().newInstance();
            }
            else {
                throw new Exception("Qualifier type '"+qualifier.value().getName()+"' is not assignable to '"+parameter.getName()+"'");
            }
        }

        Class<?> assignableClass = lookupQualifierByType(beanClassRegistry, parameter);

        if (assignableClass == null) {
            throw new Exception("Assignable bean Not Found");
        }


        return assignableClass.getDeclaredConstructor().newInstance();
    }
    public static Class<?> lookupQualifier(Set<Class<?>> beanClassRegistry, Field field) throws Exception {

        Class<?> assignableClass;
        String fieldName = field.getName();

        if ( (assignableClass = lookupQualifierByQualifierAnnotation(field)) != null) {
            return assignableClass;
        }
        else if ( (assignableClass = lookupQualifierByName(beanClassRegistry, fieldName)) != null)  {
            return assignableClass;
        }
        else if ( (assignableClass = lookupQualifierByType(beanClassRegistry, field.getType())) != null)  {
            return assignableClass;
        }

        return null;
    }

    public static Class<?> lookupQualifierByQualifierAnnotation(Field field) throws Exception {
        if (!isQualifierAnnotated(field))
            return null;

        Qualifier qualifier = field.getAnnotation(Qualifier.class);
        if (field.getType().isAssignableFrom(qualifier.value()))
            return qualifier.value();

        throw new Exception("Qualifier type '"+qualifier.value().getName()+"' is not assignable to '"+field.getType()+"'");
    }

    protected static Class<?> lookupQualifierByName(Set<Class<?>> beanClassRegistry, String name) {
        return beanClassRegistry.stream().filter(c->name.equalsIgnoreCase(c.getSimpleName()))
                .collect(toSingleton());
    }

    protected static Class<?> lookupQualifierByType(Set<Class<?>> beanClassRegistry, Class<?> type) {
        return beanClassRegistry.stream().filter(type::isAssignableFrom).collect(toSingleton());
    }

    public static <U> boolean matchParameterTypes(Class<?>[] parameterTypes, Object ...args) {

        if (parameterTypes.length != args.length) {
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }

        return true;
    }

    public static <U> Method getAutoInstantiationMethod(Class<U> cls) {
        return (Method) Arrays.stream(cls.getDeclaredMethods())
                .filter(DependencyInjector::isAutoInstantiationAnnotated)
                .filter(DependencyInjector::isStatic)
                .collect(toSingleton());
    }

    @SuppressWarnings("unchecked")
    public static <U> Constructor<U> getAutoInjectableConstructor(Class<U> cls) {
        try {
            return Objects.requireNonNullElse(
                    (Constructor<U>) Arrays.stream(cls.getConstructors())
                        .filter(DependencyInjector::isAutoInjectedAnnotated).collect(toSingleton())
                    , cls.getConstructor());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <U> Constructor<U> getConstructor(Class<U> cls, Object ...args) {

        return (Constructor<U>) Arrays.stream(cls.getConstructors())
                .filter(c-> matchParameterTypes(c.getParameterTypes(), args))
                .collect(toSingleton());
    }

    public static void warnIfNotInterface(Class<?> dependencyType) {
        if (!dependencyType.isInterface()) {
            logger.warn("   * "+dependencyType.getCanonicalName()+": Is not an Interface.");
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public static <T> Collector<T, Reference<T>, T> toSingleton() {

        return new SingletonCollector<T>();
    }
}

final class Reference<T> {

    private T value;


    public Reference(T value) {
        this.value = value;
    }

    public Reference() {

    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }



}

class SingletonCollector <T>
        implements Collector<T, Reference<T>, T> {

    private int counter = 0;

    @Override
    public Supplier<Reference<T>> supplier() {
        return Reference::new;
    }

    @Override
    public BiConsumer<Reference<T>, T> accumulator() {
        counter++;
        return Reference::set;
    }

    @Override
    public BinaryOperator<Reference<T>> combiner() {
        counter++;
        return (ref1, ref2) -> ref1;
    }

    @Override
    public Function<Reference<T>, T> finisher() {
        if (counter > 1)
            throw new IllegalStateException("Stream not singleton");
        return Reference::get;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return null;
    }

    public static <T> SingletonCollector<T> collect(Class<T> cls) {
        return new SingletonCollector<>();
    };
}


