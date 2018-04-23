package com.github.datalking.beans.factory;

/**
 * @author yaoo on 4/19/18
 */
public interface FactoryBean<T> {

    T getObject();

    Class<?> getObjectType();

    boolean isSingleton();

}
