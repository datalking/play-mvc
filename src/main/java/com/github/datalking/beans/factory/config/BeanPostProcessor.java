package com.github.datalking.beans.factory.config;

/**
 * 容器扩展接口
 * <p>
 * 在Bean实例化后，初始化前后执行，可以实现自动注入、各种代理(AOP)等功能
 *
 * @author yaoo on 4/16/18
 */
public interface BeanPostProcessor {

    // 初始化前执行
    Object postProcessBeforeInitialization(Object bean, String beanName);

    // 初始化后执行
    Object postProcessAfterInitialization(Object bean, String beanName);

}
