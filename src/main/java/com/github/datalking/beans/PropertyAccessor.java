package com.github.datalking.beans;

import com.github.datalking.common.convert.descriptor.TypeDescriptor;

import java.util.Map;

/**
 * 通过名称访问对象的属性 接口
 *
 * @author yaoo on 4/29/18
 */
public interface PropertyAccessor {

    String NESTED_PROPERTY_SEPARATOR = ".";
    char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

    String PROPERTY_KEY_PREFIX = "[";
    char PROPERTY_KEY_PREFIX_CHAR = '[';

    String PROPERTY_KEY_SUFFIX = "]";
    char PROPERTY_KEY_SUFFIX_CHAR = ']';

    boolean isReadableProperty(String propertyName);

    boolean isWritableProperty(String propertyName);

    Class<?> getPropertyType(String propertyName);

    TypeDescriptor getPropertyTypeDescriptor(String propertyName);

    Object getPropertyValue(String propertyName);

    void setPropertyValue(String propertyName, Object value);

    void setPropertyValue(PropertyValue pv);

    void setPropertyValues(Map<?, ?> map);

    void setPropertyValues(PropertyValues pvs);

    void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown);

    void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid);

}
