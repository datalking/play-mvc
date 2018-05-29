package com.github.datalking.exception;

/**
 * @author yaoo on 5/29/18
 */
public class BeanInstantiationException extends BeansException {

    private Class beanClass;

    public BeanInstantiationException(Class beanClass, String msg) {
        this(beanClass, msg, null);
    }

    public BeanInstantiationException(Class beanClass, String msg, Throwable cause) {
        super("Could not instantiate bean class [" + beanClass.getName() + "]: " + msg, cause);
        this.beanClass = beanClass;
    }

    public Class getBeanClass() {
        return beanClass;
    }

}
