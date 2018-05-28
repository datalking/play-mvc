package com.github.datalking.beans;

import com.github.datalking.common.MethodParameter;

import java.lang.reflect.Field;

/**
 * 数据类型转换 接口
 * 基于propertyEditor
 *
 * @author yaoo on 5/9/18
 */
public interface TypeConverter {

    // 将value转换成requiredType类型
    <T> T convertIfNecessary(Object value, Class<T> requiredType);

    // 将value转换成requiredType类型，方法参数常是转换的目标
    <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam);

    // 将value转换成requiredType类型，反射的字段常是转换的目标
    <T> T convertIfNecessary(Object value, Class<T> requiredType, Field field);

}
