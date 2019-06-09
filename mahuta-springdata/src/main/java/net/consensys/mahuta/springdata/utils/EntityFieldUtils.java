package net.consensys.mahuta.springdata.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.consensys.mahuta.core.utils.lamba.Throwing;
import net.consensys.mahuta.springdata.model.EntityField;

public class EntityFieldUtils {

    private EntityFieldUtils() {}
    

    public static <E, A extends Annotation, T> Optional<EntityField> extractOptionalSingleAnnotatedField(Class<E> entityClass, Class<A> annotationClass, Class<T> expectedType) {
        Optional<EntityField> entityField = extractOptionalSingleAnnotatedField(entityClass, annotationClass);
        
        if(entityField.isPresent() && !entityField.get().getType().equals(expectedType)) {
            throw new RuntimeException(String.format("Expected to find one annotation %s in the class %s typed %s (found type %s)", annotationClass.getSimpleName(), entityClass.getSimpleName(), expectedType.getSimpleName(), entityField.get().getType()));
        }
        
        return entityField;
    }
    

    
    public static <E, A extends Annotation> Optional<EntityField> extractOptionalSingleAnnotatedField(Class<E> entityClass, Class<A> annotationClass) {
        List<EntityField> entityFields = extractMultipleAnnotatedFields(entityClass, annotationClass);
                
        if(entityFields.size() > 1) {
            throw new RuntimeException(String.format("Expected to find only ONE annotation %s in the class %s (found %s)", annotationClass.getSimpleName(), entityClass.getSimpleName(), entityFields.size()));
        }
        
        if(entityFields.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(entityFields.get(0));
            
        }
    }
    
    public static <E, A extends Annotation> EntityField extractUniqueSingleAnnotatedField(Class<E> entityClass, Class<A> annotationClass) {
        Optional<EntityField> entityField = extractOptionalSingleAnnotatedField(entityClass, annotationClass);
        
        if(!entityField.isPresent()) {
            throw new RuntimeException(String.format("Expected to find annotation %s in the class %s", annotationClass.getSimpleName(), entityClass.getSimpleName()));
        }
        
        return entityField.get();
    }

    public static <E, A extends Annotation> List<EntityField> extractMultipleAnnotatedFields(Class<E> entityClass, Class<A> annotationClass) {
        
        return Arrays.asList(entityClass.getDeclaredFields())
            .stream()
            .filter(field -> field.isAnnotationPresent(annotationClass))
            .map(Throwing.rethrowFunc(field -> {
                String originalName = field.getName();
                String name = Arrays.asList(annotationClass.getDeclaredMethods())
                        .stream()
                        .filter(method -> method.getName().equals("value"))
                        .findFirst()
                        .map(Throwing.rethrowFunc(method -> (String) method.invoke(field.getAnnotation(annotationClass))))
                        .filter(n -> n != null && !n.equalsIgnoreCase(""))
                        .orElseGet(field::getName);
                Class<?> type =field.getType();
                Method setter = buildSetter(entityClass, originalName, type);
                Method getter = buildGetter(entityClass, originalName);
               return EntityField.of(name, type, getter, setter); 
            })).collect(Collectors.toList());
    }

    public static <E, T> Method buildSetter(Class<E> entityClass, String name, Class<T> type) throws NoSuchMethodException {
        
        return entityClass.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), type);
    }

    public static <E> Method buildGetter(Class<E> entityClass, String name) throws NoSuchMethodException {

        return entityClass.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
    }

    
}
