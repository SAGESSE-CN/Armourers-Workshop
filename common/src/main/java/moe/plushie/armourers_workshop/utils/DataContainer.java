package moe.plushie.armourers_workshop.utils;

import moe.plushie.armourers_workshop.api.data.IAssociatedContainerKey;
import moe.plushie.armourers_workshop.api.data.IAssociatedContainerProvider;

import java.util.HashMap;
import java.util.function.Function;

public class DataContainer implements IAssociatedContainerProvider {

    private final HashMap<Object, Object> values = new HashMap<>();

    public DataContainer() {
    }

    public static <T, V> V lazy(T object, Function<T, V> supplier) {
        var provider = (IAssociatedContainerProvider) object;
        var value = provider.getAssociatedObject(Builtin.DEFAULT);
        if (value != null) {
            // noinspection unchecked
            return (V) value;
        }
        var newValue = supplier.apply(object);
        provider.setAssociatedObject(Builtin.DEFAULT, newValue);
        return newValue;
    }

    public static <T, V> V get(T object, V defaultValue) {
        var provider = (IAssociatedContainerProvider) object;
        var value = provider.getAssociatedObject(Builtin.DEFAULT);
        if (value != null) {
            // noinspection unchecked
            return (V) value;
        }
        return defaultValue;
    }

    public static <T, V> void set(T object, V value) {
        var provider = (IAssociatedContainerProvider) object;
        provider.setAssociatedObject(Builtin.DEFAULT, value);
    }

    public static <T, V> V getValue(T object, IAssociatedContainerKey<V> key) {
        var provider = (IAssociatedContainerProvider) object;
        return provider.getAssociatedObject(key);
    }

    public static <T, V> void setValue(T object, IAssociatedContainerKey<V> key, V value) {
        var provider = (IAssociatedContainerProvider) object;
        provider.setAssociatedObject(key, value);
    }


    @Override
    public <T> T getAssociatedObject(IAssociatedContainerKey<T> key) {
        var value = getValue(key);
        if (value != null) {
            return key.getType().cast(value);
        }
        return key.getDefaultValue();
    }

    @Override
    public <T> void setAssociatedObject(IAssociatedContainerKey<T> key, T value) {
        setValue(key, value);
    }

    protected void setValue(IAssociatedContainerKey<?> key, Object value) {
        values.put(key, value);
    }

    protected Object getValue(IAssociatedContainerKey<?> key) {
        return values.get(key);
    }

    public static class Builtin extends DataContainer {

        private static final DataContainerKey<Object> DEFAULT = new DataContainerKey<>("builtin", Object.class, null);

        private Object builtin;

        @Override
        protected Object getValue(IAssociatedContainerKey<?> key) {
            if (key == DEFAULT) {
                return builtin;
            } else {
                return super.getValue(key);
            }
        }

        @Override
        protected void setValue(IAssociatedContainerKey<?> key, Object value) {
            if (key == DEFAULT) {
                builtin = value;
            } else {
                super.setValue(key, value);
            }
        }
    }
}
