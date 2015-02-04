package com.dreamddeath.core.config;

import com.netflix.config.PropertyWrapper;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by CEAJ8230 on 03/02/2015.
 */
public abstract class AbstractProperty<T> implements IProperty<T> {
    private static ConcurrentMap<String,List<AbstractProperty>> _mapCallbackPerProperty=new ConcurrentHashMap<>();

    private static void addPropertyToGlobalMap(AbstractProperty prop){
        if(!_mapCallbackPerProperty.containsKey(prop.getName())){
            _mapCallbackPerProperty.putIfAbsent(prop.getName(),new CopyOnWriteArrayList<>());
        }

        _mapCallbackPerProperty.get(prop.getName()).add(prop);
    }

    private static void addPropertyFromGlobalMap(AbstractProperty prop){
        if(!_mapCallbackPerProperty.containsKey(prop.getName())){
            _mapCallbackPerProperty.putIfAbsent(prop.getName(),new CopyOnWriteArrayList<>());
        }
        _mapCallbackPerProperty.get(prop.getName()).remove(prop);
    }

    public static void fireCallback(String name,Object value){
        if(_mapCallbackPerProperty.containsKey(name)){
            for(AbstractProperty<Object> prop:_mapCallbackPerProperty.get(name)){
                for(PropertyChangedCallback<Object> callback:prop._callbacks){
                    callback.onChange(prop,prop.getValue(),value);
                }
            }
        }
    }

    private final ExtendedPropertyWrapper<T> _wrapper;
    private final List<PropertyChangedCallback<T>> _callbacks=new CopyOnWriteArrayList<>();

    public static <T> ExtendedPropertyWrapper<T> buildDefaultWrapper(String name,T defaultValue){
        return new ExtendedPropertyWrapper<T>(name,defaultValue) {
            @Override @SuppressWarnings("unchecked")
            public T getValue() {
                return prop.getCachedValue( (Class<T>)defaultValue.getClass()).get();
            }
        };
    }

    public AbstractProperty(String name, T defaultValue){
        this(buildDefaultWrapper(name,defaultValue));
    }

    public AbstractProperty(String name, T defaultValue,PropertyChangedCallback<T>callback){
        this(buildDefaultWrapper(name,defaultValue),callback);
    }


    public AbstractProperty(ExtendedPropertyWrapper<T> wrapper){
        _wrapper = wrapper;
        addPropertyToGlobalMap(this);
    }

    public AbstractProperty(ExtendedPropertyWrapper<T> wrapper,PropertyChangedCallback<T>callback){
        this(wrapper);
        addCallback(callback);
    }

    @Override
    public T getValue() {
        return _wrapper.getValue();
    }

    @Override
    public T getDefaultValue() {
        return _wrapper.getDefaultValue();
    }

    @Override
    public String getName() {
        return _wrapper.getName();
    }

    @Override
    public DateTime getLastChangedDate() {
        return new DateTime(_wrapper.getChangedTimestamp());
    }

    @Override
    public void addCallback(final PropertyChangedCallback<T> callback) {
        //final AbstractProperty<T> current = this;
        _callbacks.add(callback);
        //addPropertyCallback(this.getName(),callback);
        //_wrapper.addCallback(() -> callback.onChange(current,_wrapper.getOldValue(),current.getValue()));
    }

    @Override
    public void removeAllCallbacks() {
        _wrapper.removeAllCallbacks();
    }

    public static abstract class ExtendedPropertyWrapper<T> extends PropertyWrapper<T>{
        private T _oldValue=null;

        public ExtendedPropertyWrapper(String name,T defaultValue){
            super(name,defaultValue);
        }

        protected void propertyChanged(T newValue) {
            _oldValue = getValue();
        }


        public T getOldValue() {
            return _oldValue;
        }
    }
}
