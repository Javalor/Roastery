package io.javalor.roastery;

import io.javalor.roastery.annotation.AutoInjectd;
import io.javalor.roastery.bean.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoInjector<T> {

    private final Class<T> injectableClass;
    private final Set<Class<?>> allBeanClasses;

    private AutoInjector(Class<T> injectableClass, Set<Class<?>> allBeanClasses) {
        this.injectableClass = injectableClass;
        this.allBeanClasses = allBeanClasses;
    }

    public static <U> AutoInjector<U> forClass(Class<U> injectableClass, Set<Class<?>> allBeanClasses) {
        return new AutoInjector<U>(injectableClass, allBeanClasses);
    }

    @SuppressWarnings({"unchecked"})
    public T getClassInstance() throws Exception {

        Set<Constructor<?>> constructorSet = Arrays.stream(injectableClass.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(AutoInjectd.class))
                .collect(Collectors.toSet());

                //Arrays.stream(injectableClass.getConstructors())
                //.filter(c -> c.isAnnotationPresent(AutoInjectd.class)).collect(Collectors.toSet());

        if (constructorSet.size()>1) {
            throw new Exception("Ambiguous (more than one AutoInjected Constructor found");
        }

        T instance;
        if (constructorSet.isEmpty()) {

            instance = injectableClass.getConstructor().newInstance();
        }
        else {

            System.out.println("Found constructor");
            Constructor<?> constructor = constructorSet.iterator().next();
            boolean access = constructor.canAccess(null);
            constructor.setAccessible(true);
            instance = (T) constructor.newInstance(buildParameter(constructor.getParameterTypes()));
            constructor.setAccessible(access);
        }


        for (Field declaredField : injectableClass.getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(AutoInjectd.class))
                continue;

            boolean access = declaredField.canAccess(instance);
            declaredField.setAccessible(true);
            
            Set<Class<?>> assignableBean = lookup(declaredField.getType());

            if (assignableBean.isEmpty()) {
                throw new Exception("Assignable bean Not Found");
            }
            if (assignableBean.size()>1) {
                throw new Exception("Ambiguous (more than one assignable bean found");
            }

            declaredField.set(instance, assignableBean.iterator().next().getDeclaredConstructor().newInstance());
            declaredField.setAccessible(access);
        }

        for (Method method : injectableClass.getDeclaredMethods()) {
            System.out.println("found method "+method.getClass().getName()+"::"+method.getName());
            if (!method.isAnnotationPresent(AutoInjectd.class))
                continue;

            boolean access = method.canAccess(instance);
            method.setAccessible(true);
            method.invoke(instance, buildParameter(method.getParameterTypes()));
            method.setAccessible(access);
        }

        return instance;
    }

    protected Object[] buildParameter(Class<?>[] parameters) throws Exception  {
        List<Object> objectList = new LinkedList<>();

        for (Class<?> parameter : parameters) {
            objectList.add(buildObject(parameter));
        }

        return objectList.toArray();
    }

    protected Object buildObject(Class<?> parameter) throws Exception {
        Set<Class<?>> assignableBean = lookup(parameter);

        if (assignableBean.isEmpty()) {
            throw new Exception("Assignable bean Not Found");
        }
        if (assignableBean.size()>1) {
            throw new Exception("Ambiguous (more than one assignable bean found");
        }

        return assignableBean.iterator().next().getDeclaredConstructor().newInstance();
    }

    protected Set<Class<?>> lookup(Class<?> dependencyType) {
        return allBeanClasses.stream().filter(dependencyType::isAssignableFrom).collect(Collectors.toSet());
    }


}
