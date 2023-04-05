package io.javalor.roastery.beanfactory;

import io.javalor.roastery.DependencyInjector;
import io.javalor.roastery.Roastery;

public class StatefulBeanFactory<T extends Class<T>> implements BeanFactory<T> {
    @Override
    public T createBean(Class<T> cls) {

        DependencyInjector<T> dependencyInjector = DependencyInjector.forClass(cls, Roastery.getBeanClassRegistry());

        dependencyInjector.
    }
}
