package com.github.datalking.aop.support;

import com.github.datalking.aop.ClassFilter;
import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.Pointcut;

/**
 * @author yaoo on 4/19/18
 */
public abstract class DynamicMethodMatcherPointcut extends DynamicMethodMatcher implements Pointcut {

    @Override
    public ClassFilter getClassFilter() {
        return ClassFilter.TRUE;
    }

    @Override
    public final MethodMatcher getMethodMatcher() {
        return this;
    }

}
