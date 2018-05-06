package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.factory.ListableBeanFactory;

/**
 * ConfigurableListableBeanFactory 接口
 * BeanFactory所有子接口的组合接口
 *
 * @author yaoo on 4/3/18
 */
public interface ConfigurableListableBeanFactory
        extends ConfigurableBeanFactory, ListableBeanFactory, AutowireCapableBeanFactory, SingletonBeanRegistry {

    BeanDefinition getBeanDefinition(String beanName);

    void preInstantiateSingletons() throws Exception;

//    void freezeConfiguration();
//    void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);


}
