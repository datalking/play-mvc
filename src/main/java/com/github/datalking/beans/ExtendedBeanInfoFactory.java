package com.github.datalking.beans;

import com.github.datalking.common.Ordered;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;

/**
 * @author yaoo on 5/28/18
 */
public class ExtendedBeanInfoFactory implements BeanInfoFactory, Ordered {

    public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {

        return (supports(beanClass) ?
                new ExtendedBeanInfo(Introspector.getBeanInfo(beanClass)) :
                null);
    }

    private boolean supports(Class<?> beanClass) {

        for (Method method : beanClass.getMethods()) {
            if (ExtendedBeanInfo.isCandidateWriteMethod(method)) {
                return true;
            }
        }

        return false;
    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
