package com.github.datalking.aop.framework;

import com.github.datalking.aop.TargetSource;
import com.github.datalking.aop.support.AopUtils;
import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 基于JDK动态代理创建代理对象
 *
 * @author yaoo on 4/18/18
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

    // 代理配置信息
    private final AdvisedSupport advised;

    public JdkDynamicAopProxy(AdvisedSupport advisedSupport) {
        this.advised = advisedSupport;
    }


    @Override
    public Object getProxy() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // 获取接口
        Class<?>[] proxiedInterfaces = AopUtils.completeProxiedInterfaces(this.advised);

        Object newObj = Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);

        return newObj;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MethodInvocation invocation;

        //代理的目标对象封装
        TargetSource targetSource = advised.targetSource;
        // 目标对象的class
        Class targetClass = null;
        // 目标对象本身
        Object target = null;


        //获取代理目标对象
        target = targetSource.getTarget();
        if (target != null) {
            targetClass = target.getClass();
        }

        //获取配置的通知Advice
        List chain = this.advised.advisorChainFactory
                .getInterceptorsAndDynamicInterceptionAdvice(advised, method, targetClass);

        Object retVal;

        //没有配置增强处理advice
        if (chain.isEmpty()) {
            //直接调用目标对象的方法
            retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);

        } else {
            //配置了advice，创建一个MethodInvocation
            invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);

            //执行通知链，沿着通知器链调用所有的通知
            retVal = invocation.proceed();
        }

        //返回值
        if (retVal != null && retVal == target) {
            //返回值为自己
            retVal = proxy;
        }
        Class<?> returnType = method.getReturnType();
        if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new Exception("Null return value from advice does not match primitive return type for: " + method);
        }

        //返回
        return retVal;

    }


}
