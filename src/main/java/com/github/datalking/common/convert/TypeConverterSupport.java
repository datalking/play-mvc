package com.github.datalking.common.convert;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.common.MethodParameter;

import java.lang.reflect.Field;

/**
 * 通过代理进行类型转换
 *
 * @author yaoo on 5/9/18
 */
public abstract class TypeConverterSupport extends PropertyEditorRegistrySupport implements TypeConverter {

    public TypeConverterDelegate typeConverterDelegate;

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType) {
        return doConvert(value, requiredType, null, null);
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) {

        return doConvert(value, requiredType, methodParam, null);
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType, Field field) {

        return doConvert(value, requiredType, null, field);
    }

    /**
     * 调用代理的方法执行类型转换
     */
    private <T> T doConvert(Object value, Class<T> requiredType, MethodParameter methodParam, Field field) {
        try {
            if (field != null) {

                return this.typeConverterDelegate.convertIfNecessary(value, requiredType, field);
            } else {

                return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParam);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}

