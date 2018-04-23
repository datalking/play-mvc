package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.PropertyValues;

import java.beans.PropertyDescriptor;

/**
 * @author yaoo on 4/18/18
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName);

    boolean postProcessAfterInstantiation(Object bean, String beanName);

    PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName);


}
