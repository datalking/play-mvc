package com.github.datalking.aop.framework;

import com.github.datalking.aop.TargetSource;

/**
 * 代理创建 入口类
 *
 * @author yaoo on 4/18/18
 */
public class ProxyFactory extends ProxyCreatorSupport {

    public ProxyFactory() {
    }

    public Object getProxy() {

        //创建一个AOP代理
        AopProxy proxy = createAopProxy();

        //返回代理
        return proxy.getProxy();
    }


    public static Object getProxy(TargetSource targetSource) {
        if (targetSource.getTargetClass() == null) {
            throw new IllegalArgumentException("Cannot create class proxy for TargetSource with null target class");
        }

        ProxyFactory proxyFactory = new ProxyFactory();
        // 设置要为之创建代理的对象
        proxyFactory.setTargetSource(targetSource);
        //proxyFactory.setProxyTargetClass(true);
        return proxyFactory.getProxy();
    }


}
