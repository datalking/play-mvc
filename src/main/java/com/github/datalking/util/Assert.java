package com.github.datalking.util;

/**
 * 判断型工具类
 *
 * @author yaoo on 4/3/18
 */
public interface Assert {


    static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(message + subType + " is not assignable to " + superType);
        }
    }

}
