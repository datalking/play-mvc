package com.github.datalking.exception;

import com.github.datalking.util.ClassUtils;

import java.beans.PropertyChangeEvent;

/**
 * @author yaoo on 5/29/18
 */
public class TypeMismatchException extends PropertyAccessException {

    public static final String ERROR_CODE = "typeMismatch";

    private transient Object value;

    private Class requiredType;

    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType) {
        this(propertyChangeEvent, requiredType, null);
    }

    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType, Throwable cause) {
        super(propertyChangeEvent, "Failed to convert property value of type '" +
                ClassUtils.getDescriptiveType(propertyChangeEvent.getNewValue()) + "'" +
                (requiredType != null ? " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : "") +
                (propertyChangeEvent.getPropertyName() != null ? " for property '" + propertyChangeEvent.getPropertyName() + "'" : ""), cause);
        this.value = propertyChangeEvent.getNewValue();
        this.requiredType = requiredType;
    }

    public TypeMismatchException(Object value, Class requiredType) {
        this(value, requiredType, null);
    }

    public TypeMismatchException(Object value, Class requiredType, Throwable cause) {
        super("Failed to convert value of type '" + ClassUtils.getDescriptiveType(value) + "'" + (requiredType != null ? " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : ""), cause);
        this.value = value;
        this.requiredType = requiredType;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public Class getRequiredType() {
        return this.requiredType;
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }

}
