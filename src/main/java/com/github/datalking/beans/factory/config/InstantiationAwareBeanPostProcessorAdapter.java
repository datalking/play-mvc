package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.PropertyValues;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;

/**
 * @author yaoo on 5/28/18
 */
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor {

    public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
        return null;
    }

    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) {
        return null;
    }

    public Object getEarlyBeanReference(Object bean, String beanName) {
        return bean;
    }

    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        return null;
    }

    public boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }

    public PropertyValues postProcessPropertyValues(PropertyValues pvs,
                                                    PropertyDescriptor[] pds,
                                                    Object bean,
                                                    String beanName) {

        return pvs;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
