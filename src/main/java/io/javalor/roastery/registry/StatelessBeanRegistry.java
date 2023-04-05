package io.javalor.roastery.registry;

public class StatelessBeanRegistry extends BeanRegistry {
    public StatelessBeanRegistry() {
    }

    public StatelessBeanRegistry(int initialCapacity) {
        super(initialCapacity);
    }

    public StatelessBeanRegistry(BeanRegistry m) {
        super(m);
    }

    public StatelessBeanRegistry(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StatelessBeanRegistry(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }
}
