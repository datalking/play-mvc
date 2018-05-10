package com.github.datalking.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * 包装bean属性的接口
 *
 * @author yaoo on 4/3/18
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor {

    Object getWrappedInstance();

    Class<?> getWrappedClass();

    void setPropertyValues(PropertyValues pvs);

    PropertyDescriptor[] getPropertyDescriptors();

    PropertyDescriptor getPropertyDescriptor(String propertyName);

//    void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);
//    boolean isAutoGrowNestedPaths();
//    void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);
//    int getAutoGrowCollectionLimit();


}
