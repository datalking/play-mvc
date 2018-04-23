package com.github.datalking.aop.framework;

import com.github.datalking.aop.MethodMatcher;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author yaoo on 4/19/18
 */
public class InterceptorAndDynamicMethodMatcher {

    final MethodInterceptor interceptor;

    final MethodMatcher methodMatcher;

    public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
        this.interceptor = interceptor;
        this.methodMatcher = methodMatcher;
    }

}
