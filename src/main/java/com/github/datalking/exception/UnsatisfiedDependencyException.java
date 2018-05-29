package com.github.datalking.exception;

import com.github.datalking.util.ClassUtils;

/**
 * @author yaoo on 5/29/18
 */
public class UnsatisfiedDependencyException extends BeanCreationException {

    public UnsatisfiedDependencyException(String resourceDescription, String beanName, String propertyName, String msg) {

        super(resourceDescription, beanName,
                "Unsatisfied dependency expressed through bean property '" + propertyName + "'" +
                        (msg != null ? ": " + msg : ""));
    }

    public UnsatisfiedDependencyException(String resourceDescription, String beanName, String propertyName, BeansException ex) {

        this(resourceDescription, beanName, propertyName, (ex != null ? ": " + ex.getMessage() : ""));
        initCause(ex);
    }

    public UnsatisfiedDependencyException(String resourceDescription, String beanName, int ctorArgIndex, Class ctorArgType, String msg) {

        super(resourceDescription, beanName, "Unsatisfied dependency expressed through constructor argument with index " +
                ctorArgIndex + " of type [" + ClassUtils.getQualifiedName(ctorArgType) + "]" +
                (msg != null ? ": " + msg : ""));
    }

    public UnsatisfiedDependencyException(String resourceDescription, String beanName, int ctorArgIndex, Class ctorArgType, BeansException ex) {

        this(resourceDescription, beanName, ctorArgIndex, ctorArgType, (ex != null ? ": " + ex.getMessage() : ""));
        initCause(ex);
    }

}
