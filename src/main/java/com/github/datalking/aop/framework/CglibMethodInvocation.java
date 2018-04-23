package com.github.datalking.aop.framework;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * AOP Alliance MethodInvocation的实现类
 */
public class CglibMethodInvocation extends ReflectiveMethodInvocation {

    private final MethodProxy methodProxy;

    private final boolean publicMethod;

    CglibMethodInvocation(Object proxy,
                          Object target,
                          Method method,
                          Object[] arguments,
                          Class<?> targetClass,
                          List<Object> interceptorsAndDynamicMethodMatchers,
                          MethodProxy methodProxy) {

        super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
        this.methodProxy = methodProxy;
        this.publicMethod = Modifier.isPublic(method.getModifiers());
    }

    /**
     * public方法使用cglib调用
     */
    @Override
    protected Object invokeJoinpoint() throws Throwable {
        if (this.publicMethod) {
            return this.methodProxy.invoke(this.target, this.arguments);
        } else {
            return super.invokeJoinpoint();
        }
    }


}
