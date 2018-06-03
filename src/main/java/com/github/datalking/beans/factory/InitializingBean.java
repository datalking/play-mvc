package com.github.datalking.beans.factory;

/**
 * 定义afterPropertiesSet()，在bean注入属性后执行
 *
 * @author yaoo on 4/26/18
 */
public interface InitializingBean {

    // populateBean()注入属性后执行
    void afterPropertiesSet();

}
