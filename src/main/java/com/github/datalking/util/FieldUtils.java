package com.github.datalking.util;

import java.lang.reflect.Field;

/**
 * @author yaoo on 6/9/18
 */
public abstract class FieldUtils {

//    public static List<Field> getDeclaredFieldsUninitialized(Class clazz){
//        Assert.notNull(clazz,"input class args cannot null");
//        Field[] fields = clazz.getDeclaredFields();
//    }

    public static boolean isBasicClassType(Field field) {
        return field.getType().isPrimitive();
    }

    public static boolean isTargetClassType(Field field, Class targetType) {
        return field.getType() == targetType;
    }

}
