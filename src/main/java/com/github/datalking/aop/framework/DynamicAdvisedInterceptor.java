package com.github.datalking.aop.framework;


import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 基于CGLIB实现动态织入
 *
 * @author yaoo on 4/19/18
 */
public class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

    private final AdvisedSupport advised;

    public DynamicAdvisedInterceptor(AdvisedSupport advised) {
        this.advised = advised;
    }

    /**
     * 为Bean的切点方法进行Advisor动态织入
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Class<?> targetClass = null;
        Object target = advised.getTargetSource().getTarget();
        if (target != null) {
            targetClass = target.getClass();
        }

        // 获取拦截器链
        List<Object> chain = advised.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(advised, method, targetClass);
        Object retVal;

        // 如果没有拦截器链，则直接调用目标类的方法
        if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {

            // Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
            retVal = methodProxy.invoke(target, args);

        } else {
            // 构造CglibMethodInvocation，递归调用拦截器链
            retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
        }

        if (retVal != null && retVal == target) {
            retVal = proxy;
        }
        Class<?> returnType = method.getReturnType();
        if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new Exception("Null return value from advice does not match primitive return type for: " + method);
        }

        return retVal;

    }

    @Override
    public boolean equals(Object other) {
        return (this == other ||
                (other instanceof DynamicAdvisedInterceptor && this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
    }

    @Override
    public int hashCode() {
        return this.advised.hashCode();
    }


}
