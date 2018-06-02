package com.github.datalking.exception;

/**
 * @author yaoo on 6/2/18
 */
public class BeanCurrentlyInCreationException extends BeanCreationException {

    public BeanCurrentlyInCreationException(String beanName) {
        super(beanName, "Requested bean is currently in creation: Is there an unresolvable circular reference?");
    }

    public BeanCurrentlyInCreationException(String beanName, String msg) {
        super(beanName, msg);
    }

}
