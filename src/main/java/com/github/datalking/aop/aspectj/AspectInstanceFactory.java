package com.github.datalking.aop.aspectj;


import com.github.datalking.common.Ordered;

/**
 * @author yaoo on 4/19/18
 */
public interface AspectInstanceFactory extends Ordered {

    Object getAspectInstance();

    //ClassLoader getAspectClassLoader();

}
