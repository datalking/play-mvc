package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.BeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author yaoo on 5/29/18
 */
public interface InstantiationStrategy {

    Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner);

    Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
                       Constructor<?> ctor, Object[] args);

    Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
                       Object factoryBean, Method factoryMethod, Object[] args);

}

