package com.github.datalking.aop;

import java.lang.reflect.Method;

/**
 * @author yaoo on 4/18/18
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

    // 在给定的方法调用前，调用before方法
    void before(Method method, Object[] args, Object target);


}
