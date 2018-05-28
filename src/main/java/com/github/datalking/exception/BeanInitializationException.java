package com.github.datalking.exception;

/**
 * @author yaoo on 5/28/18
 */
public class BeanInitializationException extends BeansException {

    public BeanInitializationException(String msg) {
        super(msg);
    }

    public BeanInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
