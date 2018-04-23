package com.github.datalking.aop.aspectj.jadvice;

import com.github.datalking.aop.MethodBeforeAdvice;
import com.github.datalking.aop.aspectj.AspectInstanceFactory;
import com.github.datalking.aop.aspectj.AspectJExpressionPointcut;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 */
@SuppressWarnings("serial")
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice, Serializable {

    public AspectJMethodBeforeAdvice(Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

        super(aspectJBeforeAdviceMethod, pointcut, aif);
    }


    @Override
    public void before(Method method, Object[] args, Object target) {

        try {
            invokeAdviceMethod(getJoinPointMatch(), null, null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public boolean isBeforeAdvice() {
        return true;
    }

    @Override
    public boolean isAfterAdvice() {
        return false;
    }

}
