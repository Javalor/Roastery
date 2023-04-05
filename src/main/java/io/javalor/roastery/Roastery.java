package io.javalor.roastery;

import io.javalor.componentscanner.ComponentScanner;
import io.javalor.roastery.registry.StatelessBeanRegistry;

import java.util.Objects;
import java.util.Set;

public class Roastery {

    private static final Roastery ROASTERY_INSTANCE;
    private static final StatelessBeanRegistry STATELESS_BEAN_REGISTRY = new StatelessBeanRegistry();
    private static final Set<Class<?>> BEAN_CLASS_REGISTRY;

    static {
        ComponentScanner componentScanner = new ComponentScanner();
        BEAN_CLASS_REGISTRY = componentScanner.getScannedClass();
        ROASTERY_INSTANCE = new Roastery();
    }

    private Roastery() {

    }

    public static Roastery getManager() {
        return ROASTERY_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <U> U lookupStatelessBeanRegistry(Class<U> cls) {
            return (U) STATELESS_BEAN_REGISTRY.getOrDefault(cls, null);
    }

    @SuppressWarnings("unchecked")
    public <U> U registerStatelessBean(U obj) {
        Objects.requireNonNull(obj, "Cannot register 'null' as a singleton.");
        Class<U> cls = (Class<U>) obj.getClass();

        return (U) STATELESS_BEAN_REGISTRY.putIfAbsent(cls, obj);
    }

    public static Set<Class<?>> getBeanClassRegistry() {
        return BEAN_CLASS_REGISTRY;
    }

    public Class<?> lookupQualifierByType(Class<?> type) {
        return DependencyInjector.lookupQualifierByType(BEAN_CLASS_REGISTRY, type);
    }

    public <U> U getBean(Class<U> cls) {
        DependencyInjector.forClass(cls, BEAN_CLASS_REGISTRY);
        return null;
    }







}
