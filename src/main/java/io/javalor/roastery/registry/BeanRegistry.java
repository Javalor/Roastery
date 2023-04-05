package io.javalor.roastery.registry;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BeanRegistry implements MappedRegistry<Class<?>, Object>,  ValueRegistry<Object> {

    private final Map<Class<?>, Object> registry;

    public BeanRegistry() {
        registry = new ConcurrentHashMap<>();
    }

    public BeanRegistry(int initialCapacity) {
        registry = new ConcurrentHashMap<>(initialCapacity);
    }

    public BeanRegistry(BeanRegistry m) {
        registry = new ConcurrentHashMap<>(m.registry);
    }

    public BeanRegistry(int initialCapacity, float loadFactor) {
        registry = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    public BeanRegistry(int initialCapacity, float loadFactor, int concurrencyLevel) {
        registry = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public int size() {
        return registry.size();
    }

    public boolean isEmpty() {
        return registry.isEmpty();
    }

    public boolean containsKey(Object key) {
        return registry.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return registry.containsValue(value);
    }

    public boolean contains(Object value) {
        return containsValue(value);
    }

    @Override
    public Iterator<Object> iterator() {
        return values().iterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Object o) {
        return false;
    }

    public boolean contains(Class<?> key) {
        return containsKey(key);
    }

    public Object get(Object key) {
        return registry.get(key);
    }

    public Object put(Class<?> key, Object value) {
        return registry.put(key, value);
    }

    public Object remove(Object key) {
        return registry.remove(key);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public void putAll(Map<? extends Class<?>, ? extends Object> m) {
        registry.putAll(m);
    }

    public void clear() {
        registry.clear();
    }

    public Set<Class<?>> keySet() {
        return registry.keySet();
    }

    public Collection<Object> values() {
        return registry.values();
    }

    public Set<Map.Entry<Class<?>, Object>> entrySet() {
        return registry.entrySet();
    }

    public Object getOrDefault(Object key, Object defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    public void forEach(BiConsumer<? super Class<?>, ? super Object> action) {
        registry.forEach(action);
    }

    public void replaceAll(BiFunction<? super Class<?>, ? super Object, ? extends Object> function) {
        registry.replaceAll(function);
    }

    public Object putIfAbsent(Class<?> key, Object value) {
        return registry.putIfAbsent(key, value);
    }

    public boolean remove(Object key, Object value) {
        return registry.remove(key, value);
    }

    public boolean replace(Class<?> key, Object oldValue, Object newValue) {
        return registry.replace(key, oldValue, newValue);
    }

    public Object replace(Class<?> key, Object value) {
        return registry.replace(key, value);
    }

    public Object computeIfAbsent(Class<?> key, Function<? super Class<?>, ? extends Object> mappingFunction) {
        return registry.computeIfAbsent(key, mappingFunction);
    }

    public Object computeIfPresent(Class<?> key, BiFunction<? super Class<?>, ? super Object, ? extends Object> remappingFunction) {
        return registry.computeIfPresent(key, remappingFunction);
    }

    public Object compute(Class<?> key, BiFunction<? super Class<?>, ? super Object, ? extends Object> remappingFunction) {
        return registry.compute(key, remappingFunction);
    }

    public Object merge(Class<?> key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return registry.merge(key, value, remappingFunction);
    }


}
