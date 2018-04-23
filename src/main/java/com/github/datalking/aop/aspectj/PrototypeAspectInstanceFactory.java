package com.github.datalking.aop.aspectj;

import com.github.datalking.beans.factory.BeanFactory;

import java.io.Serializable;

/**
 * @author yaoo on 4/19/18
 */
public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory implements Serializable {

    public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
        super(beanFactory, name);

//        if (!beanFactory.isPrototype(name)) {
//            throw new IllegalArgumentException("Cannot use PrototypeAspectInstanceFactory with bean named '" + name + "': not a prototype");
//        }
    }

}
