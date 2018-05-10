package com.github.datalking.beans;

/**
 * 工具类
 *
 * @author yaoo on 5/10/18
 */
public abstract class PropertyAccessorFactory {

    public static BeanWrapper forBeanPropertyAccess(Object target) {
        return new BeanWrapperImpl(target);
    }

    public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target) {
        return new DirectFieldAccessor(target);
    }

}
