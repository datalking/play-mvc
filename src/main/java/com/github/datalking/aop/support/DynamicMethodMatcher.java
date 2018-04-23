package com.github.datalking.aop.support;

import com.github.datalking.aop.MethodMatcher;

import java.lang.reflect.Method;

/**
 * @author yaoo on 4/19/18
 */
public abstract class DynamicMethodMatcher implements MethodMatcher {

    @Override
    public final boolean isRuntime() {
        return true;
    }

    /**
     * 用于扩展
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return true;
    }

}
