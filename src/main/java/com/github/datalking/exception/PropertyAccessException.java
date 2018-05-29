package com.github.datalking.exception;

import java.beans.PropertyChangeEvent;

/**
 * @author yaoo on 5/29/18
 */
public abstract class PropertyAccessException extends BeansException {

    private transient PropertyChangeEvent propertyChangeEvent;

    public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, Throwable cause) {
        super(msg, cause);
        this.propertyChangeEvent = propertyChangeEvent;
    }

    public PropertyAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public PropertyChangeEvent getPropertyChangeEvent() {
        return this.propertyChangeEvent;
    }

    public String getPropertyName() {
        return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getPropertyName() : null);
    }

    public Object getValue() {
        return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getNewValue() : null);
    }

}
