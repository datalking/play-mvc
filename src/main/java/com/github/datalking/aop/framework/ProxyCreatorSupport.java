package com.github.datalking.aop.framework;

import com.github.datalking.util.Assert;

/**
 * 提供创建代理的方法
 *
 * @author yaoo on 4/18/18
 */
public class ProxyCreatorSupport extends AdvisedSupport {


    private AopProxyFactory aopProxyFactory;

    // 创建一个代理后就会设为true
    private boolean active = false;

    public ProxyCreatorSupport() {
        this.aopProxyFactory = new DefaultAopProxyFactory();
    }

    public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
        Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
        this.aopProxyFactory = aopProxyFactory;
    }

    public AopProxyFactory getAopProxyFactory() {
        return aopProxyFactory;
    }

    public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
        this.aopProxyFactory = aopProxyFactory;
    }


    protected final synchronized AopProxy createAopProxy() {
        if (!this.active) {
            activate();
        }

        return getAopProxyFactory().createAopProxy(this);
    }

    private void activate() {
        this.active = true;
//        for (AdvisedSupportListener listener : this.listeners) {
//            listener.activated(this);
//        }
    }

    protected final synchronized boolean isActive() {
        return this.active;
    }

}
