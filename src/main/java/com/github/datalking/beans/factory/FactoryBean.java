package com.github.datalking.beans.factory;

/**
 * FactoryBean可以创建Bean对象的工厂Bean
 * FactoryBean接口是IOC容器的实例化逻辑的可插拔点，方便使用java编程方式实现较复杂的bean实例化
 * 使用场景包括aop、事务管理等
 *
 * @author yaoo on 4/19/18
 */
public interface FactoryBean<T> {

    // 返回工厂创建的bean实例
    T getObject();

    // 返回创建bean的类型
    Class<?> getObjectType();

    // 创建的对象是否单例
    boolean isSingleton();

}
