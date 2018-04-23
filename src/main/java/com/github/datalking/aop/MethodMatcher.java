package com.github.datalking.aop;

import java.lang.reflect.Method;

/**
 * @author yaoo on 4/18/18
 */
public interface MethodMatcher {

    // 是否是需要匹配的静态方法
    boolean matches(Method method, Class<?> targetClass);

    //是否是运行时动态匹配 默认false
     boolean isRuntime();

    // 是否是需要匹配的动态方法
     boolean matches(Method method, Class<?> targetClass, Object... args);

    MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;


}
