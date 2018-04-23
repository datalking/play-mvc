package com.github.datalking.aop.support;

import com.github.datalking.aop.ClassFilter;
import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.Pointcut;

/**
 * 静态方法pointcut
 *
 * @author yaoo on 4/19/18
 */
public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

    private ClassFilter classFilter = ClassFilter.TRUE;

    public void setClassFilter(ClassFilter classFilter) {
        this.classFilter = classFilter;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this.classFilter;
    }


    @Override
    public final MethodMatcher getMethodMatcher() {
        return this;
    }

}
