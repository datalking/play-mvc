package com.github.datalking.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

/**
 * @author yaoo on 5/28/18
 */
public interface BeanInfoFactory {

    BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;

}
