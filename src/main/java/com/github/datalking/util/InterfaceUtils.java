package com.github.datalking.util;

/**
 * @author yaoo on 6/9/18
 */
public abstract class InterfaceUtils {

    public static Class<?>[] getAllInterfaces(Class<?> clazz) {
        Assert.notNull(clazz, "input class args cannot be null.");

        return clazz.getInterfaces();
    }

}
