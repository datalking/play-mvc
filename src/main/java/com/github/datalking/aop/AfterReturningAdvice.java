package com.github.datalking.aop;

import java.lang.reflect.Method;

/**
 * @author yaoo on 4/18/18
 */
public interface AfterReturningAdvice extends AfterAdvice {

    // 在方法正常返回前进行增强处理，该增强可以看到方法的返回值，但是不能更改返回值
    void afterReturning(Object returnValue, Method method, Object[] args, Object target);

}
