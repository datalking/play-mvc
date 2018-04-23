package com.github.datalking.aop;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author yaoo on 4/18/18
 */
public class TrueMethodMatcher implements MethodMatcher, Serializable {

    public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

    private TrueMethodMatcher() {
    }


    @Override
    public boolean isRuntime() {
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return true;
    }


    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        throw new UnsupportedOperationException();
    }

    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "MethodMatcher.TRUE";
    }

}
