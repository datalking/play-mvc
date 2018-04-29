package com.github.datalking.exception;

import com.github.datalking.beans.PropertyAccessor;

import java.util.List;

/**
 */
public interface Errors {

    // 默认为点号.
    String NESTED_PATH_SEPARATOR = PropertyAccessor.NESTED_PROPERTY_SEPARATOR;

    String getObjectName();

    void setNestedPath(String nestedPath);

    String getNestedPath();

    void pushNestedPath(String subPath);

    void popNestedPath() throws IllegalStateException;

    void reject(String errorCode);

    void reject(String errorCode, String defaultMessage);

    void reject(String errorCode, Object[] errorArgs, String defaultMessage);

    void rejectValue(String field, String errorCode);

    void rejectValue(String field, String errorCode, String defaultMessage);

    void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage);

    void addAllErrors(Errors errors);

    boolean hasErrors();

    int getErrorCount();

    List<ObjectError> getAllErrors();

    boolean hasGlobalErrors();

    int getGlobalErrorCount();

    List<ObjectError> getGlobalErrors();

    ObjectError getGlobalError();

    boolean hasFieldErrors();

    int getFieldErrorCount();

    List<FieldError> getFieldErrors();

    FieldError getFieldError();

    boolean hasFieldErrors(String field);

    int getFieldErrorCount(String field);

    List<FieldError> getFieldErrors(String field);

    FieldError getFieldError(String field);

    Object getFieldValue(String field);

    Class<?> getFieldType(String field);

}
