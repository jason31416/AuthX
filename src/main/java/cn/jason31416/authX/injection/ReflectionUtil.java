package cn.jason31416.authX.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Object fetchField(Object obj, String fieldName, Class<?> clazz){
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        }catch (Exception e){
            throw new ReflectionException("Failed to fetch field " + fieldName + " from object " + obj, e);
        }
    }

    public static Method handleAccessible(Method method) {
        method.setAccessible(true);
        return method;
    }

    public static Field handleAccessible(Field field) {
        field.setAccessible(true);
        return field;
    }

    public static<T> Constructor<T> handleAccessible(Constructor<T> constructor) {
        constructor.setAccessible(true);
        return constructor;
    }
}
