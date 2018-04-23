package com.github.datalking.aop.framework;

/**
 * aop代理创建工厂 接口
 *
 * @author yaoo on 4/18/18
 */
public interface AopProxyFactory {

    AopProxy createAopProxy(AdvisedSupport config);

}
