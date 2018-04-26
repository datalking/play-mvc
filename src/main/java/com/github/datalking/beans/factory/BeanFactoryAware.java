package com.github.datalking.beans.factory;

/**
 * @author yaoo on 4/18/18
 */
public interface BeanFactoryAware extends Aware {

    void setBeanFactory(BeanFactory beanFactory);

}
