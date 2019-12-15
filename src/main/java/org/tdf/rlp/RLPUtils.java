package org.tdf.rlp;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

final class RLPUtils {
    static RLPEncoder getAnnotatedRLPEncoder(AnnotatedElement element) {
        if (!element.isAnnotationPresent(RLPEncoding.class)) {
            return null;
        }
        Class<? extends RLPEncoder> encoder = element.getAnnotation(RLPEncoding.class).value();
        if (encoder == RLPEncoder.None.class) {
            return null;
        }
        return newInstance(encoder);
    }

    static RLPDecoder getAnnotatedRLPDecoder(AnnotatedElement element) {
        if (!element.isAnnotationPresent(RLPDecoding.class)) {
            return null;
        }
        Class<? extends RLPDecoder> decoder = element.getAnnotation(RLPDecoding.class).value();
        if (decoder == RLPDecoder.None.class) {
            return null;
        }
        return newInstance(decoder);
    }

    static List<Field> getRLPFields(Class clazz) {
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(RLP.class))
                .sorted(Comparator.comparingInt(x -> x.getAnnotation(RLP.class).value())).collect(Collectors.toList());
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getAnnotation(RLP.class).value() != i)
                throw new RuntimeException(String.format("field %s of class %s should have RLP(%d)", fields.get(i), clazz, i));
        }
        return fields;
    }

    static Comparator getContentOrdering(AnnotatedElement element) {
        if (!element.isAnnotationPresent(RLPEncoding.class)) {
            return null;
        }
        Class<? extends Comparator> clazz = element.getAnnotation(RLPEncoding.class).contentOrdering();
        if (clazz == RLPEncoding.None.class) return null;
        return newInstance(clazz);
    }

    static Comparator getKeyOrdering(AnnotatedElement element) {
        if (!element.isAnnotationPresent(RLPEncoding.class)) {
            return null;
        }
        Class<? extends Comparator> clazz = element.getAnnotation(RLPEncoding.class).keyOrdering();
        if (clazz == RLPEncoding.None.class) return null;
        return newInstance(clazz);
    }

    static Class getGenericTypeRecursively(Class clazz, final int index) {
        Optional<Class> o = Arrays.stream(clazz.getGenericInterfaces())
                .filter(x -> x instanceof ParameterizedType)
                .map(x -> (ParameterizedType) x)
                .findFirst()
                .map(x -> {
                    Type[] types = x.getActualTypeArguments();
                    if (index < types.length && types[index] instanceof Class) {
                        return (Class) types[index];
                    }
                    return null;
                });

        if (o.isPresent()) return o.get();
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (index < types.length && types[index] instanceof Class) return (Class) types[index];
        }
        if (clazz.getSuperclass() == null) return null;
        return getGenericTypeRecursively(clazz.getSuperclass(), index);
    }

    static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            return con.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(clazz + " should has an no-argument constructor");
        }
    }

    static Container fromType(Type type) {
        if (type instanceof Class) return fromClass((Class) type);
        if(type instanceof ParameterizedType) return fromParameterizedType((ParameterizedType) type);
        return null;
    }

    static Container fromClass(Class clazz) {
        if(Collection.class.isAssignableFrom(clazz)){
            // clazz my extends from a generic super class
            // clazz my implements a generic interface
        }
        if(Map.class.isAssignableFrom(clazz)){

        }
        return new Raw(clazz);
    }

    static Container fromParameterizedType(ParameterizedType type){
        Class clazz = (Class) type.getRawType();
        Container con = fromClass(clazz);
        switch (con.getType()){
            case RAW: return con;
            case MAP: {

            }
        }
        return null;
    }
}
