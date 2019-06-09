package net.consensys.mahuta.springdata.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityField {

    private String name;
    private Class<?> type;
    private Method getter;
    private Method setter;
    
    private EntityField(String name, Class<?> type, Method getter, Method setter) {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }
    
    public static EntityField of(String name, Class<?> type, Method getter, Method setter) {
        return new EntityField(name, type, getter, setter);
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }
    
    public void invokeSetter(Object entity, Object value) throws IllegalAccessException, InvocationTargetException {
        if(!type.isInstance(value)) {
            throw new RuntimeException("invokeSetter - Expected value to set of type " + type.getSimpleName() + " but was " +value.getClass().getSimpleName());
        }
        
        setter.invoke(entity, value);
    }
    
    public <T> T invokeGetter(Object entity, Class<T> type) throws IllegalAccessException, InvocationTargetException {
        Object value = getter.invoke(entity);
        
        if(value == null) {
            return null;
        }
        
        if(!type.isInstance(value)) {
            throw new RuntimeException("invokeGetter - Expected value retrieved of type " + type.getSimpleName() + " but was " + value.getClass().getSimpleName());
        }
        return type.cast(value);
    }

    
}
