package com.github.datalking.aop.framework;

import com.github.datalking.aop.support.AopUtils;
import com.github.datalking.util.Assert;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import java.io.Serializable;

/**
 * 基于CGLIB创建代理对象
 *
 * @author yaoo on 4/18/18
 */
class CglibAopProxy implements AopProxy, Serializable {

    protected final AdvisedSupport advised;
    protected Object[] constructorArgs;
    protected Class<?>[] constructorArgTypes;

//    private final transient AdvisedDispatcher advisedDispatcher;

    public CglibAopProxy(AdvisedSupport advised) {
        this.advised = advised;
//        this.advisedDispatcher = new AdvisedDispatcher(advised);
    }

    public Object getProxy() {

        // 目标对象
        Class<?> rootClass = this.advised.getTargetClass();
        Assert.notNull(rootClass, "Target class must be available for creating a CGLIB proxy");

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(rootClass);
        enhancer.setInterfaces(AopUtils.completeProxiedInterfaces(this.advised));

        // 获取类的回调列表，都是MethodInterceptor的实例
        // 最主要的 DynamicAdvisedInterceptor，这个就是动态织入增强的拦截器
        Callback[] callbacks = getCallbacks(rootClass);
        Class<?>[] types = new Class<?>[callbacks.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = callbacks[i].getClass();
        }
        enhancer.setCallbackTypes(types);
        enhancer.setCallbacks(callbacks);
        enhancer.setInterceptDuringConstruction(false);

        Object enhanced = (this.constructorArgs != null ?
                enhancer.create(this.constructorArgTypes, this.constructorArgs) :
                enhancer.create());

        return enhanced;
    }

    private Callback[] getCallbacks(Class<?> rootClass) {

        // ==== 选择aop拦截器
        Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

        Callback[] callbacks = new Callback[]{
                aopInterceptor
                //this.advisedDispatcher
        };

        return callbacks;

    }


    public void setConstructorArguments(Object[] constructorArgs, Class<?>[] constructorArgTypes) {
        if (constructorArgs == null || constructorArgTypes == null) {
            throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
        }
        if (constructorArgs.length != constructorArgTypes.length) {
            throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length +
                    ") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
        }
        this.constructorArgs = constructorArgs;
        this.constructorArgTypes = constructorArgTypes;
    }


//    private static class AdvisedDispatcher implements Dispatcher, Serializable {
//
//        private final AdvisedSupport advised;
//
//        public AdvisedDispatcher(AdvisedSupport advised) {
//            this.advised = advised;
//        }
//
//        @Override
//        public Object loadObject() throws Exception {
//            return this.advised;
//        }
//    }

}
