package com.github.datalking.aop;

/**
 * @author yaoo on 4/18/18
 */
public interface Pointcut {

    /**
     * 类过滤器，匹配需要拦截的类
     */
    ClassFilter getClassFilter();

    /**
     * 方法匹配器，匹配需要拦截的方法
     */
    MethodMatcher getMethodMatcher();

    Pointcut TRUE = TruePointcut.INSTANCE;

}
