package com.github.datalking.beans.factory;

/**
 * @author yaoo on 4/26/18
 */
public interface BeanNameAware extends Aware {

    void setBeanName(String name);

}
