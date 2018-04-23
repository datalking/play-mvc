package com.github.datalking.aop.aspectj.jadvice;

import com.github.datalking.aop.AfterAdvice;
import com.github.datalking.aop.AfterReturningAdvice;
import com.github.datalking.aop.aspectj.AspectInstanceFactory;
import com.github.datalking.aop.aspectj.AspectJExpressionPointcut;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 */
@SuppressWarnings("serial")
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice
        implements AfterReturningAdvice, AfterAdvice, Serializable {

    public AspectJAfterReturningAdvice(Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

        super(aspectJBeforeAdviceMethod, pointcut, aif);
    }


    @Override
    public boolean isBeforeAdvice() {
        return false;
    }

    @Override
    public boolean isAfterAdvice() {
        return true;
    }

    @Override
    public void setReturningName(String name) {
        setReturningNameNoCheck(name);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
        if (shouldInvokeOnReturnValueOf(method, returnValue)) {

            try {
                invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
    }


    private boolean shouldInvokeOnReturnValueOf(Method method, Object returnValue) {
        Class<?> type = getDiscoveredReturningType();
        Type genericType = getDiscoveredReturningGenericType();
        // If we aren't dealing with a raw type, check if generic parameters are assignable.
        return (matchesReturnValue(type, method, returnValue) &&
                (genericType == null || genericType == type ||
                        TypeUtils.isAssignable(genericType, method.getGenericReturnType())));
    }


    private boolean matchesReturnValue(Class<?> type, Method method, Object returnValue) {
        if (returnValue != null) {
            return ClassUtils.isAssignableValue(type, returnValue);
        } else if (Object.class == type && void.class == method.getReturnType()) {
            return true;
        } else {
            return ClassUtils.isAssignable(type, method.getReturnType());
        }
    }

}
