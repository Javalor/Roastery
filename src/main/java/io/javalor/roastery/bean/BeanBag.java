package io.javalor.roastery.bean;

import java.util.Set;

public interface BeanBag {
    boolean registerBeanInterface(Class<?> beanInterface);
    boolean registerBeanClass(Class<?> beanClass);
    Set<Class<?>> getAllBeanClasses();
}
