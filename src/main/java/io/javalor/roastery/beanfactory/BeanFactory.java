package io.javalor.roastery.beanfactory;

public interface BeanFactory<T extends Class<T>> {

    T createBean(Class<T> cls);
}
