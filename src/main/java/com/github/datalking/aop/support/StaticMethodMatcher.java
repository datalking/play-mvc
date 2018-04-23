package com.github.datalking.aop.support;

import com.github.datalking.aop.MethodMatcher;

import java.lang.reflect.Method;

/**
 * @author yaoo on 4/19/18
 */
public abstract class StaticMethodMatcher implements MethodMatcher {

    @Override
    public final boolean isRuntime() {
        return false;
    }

    @Override
    public final boolean matches(Method method, Class<?> targetClass, Object... args) {
        throw new UnsupportedOperationException("Illegal MethodMatcher usage");
    }

}
