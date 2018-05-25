package com.github.datalking.common;

import com.github.datalking.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author yaoo on 5/10/18
 */
public abstract class AbstractErrors implements Errors, Serializable {

    private String nestedPath = "";

    private final Stack<String> nestedPathStack = new Stack<>();

    public void setNestedPath(String nestedPath) {
        doSetNestedPath(nestedPath);
        this.nestedPathStack.clear();
    }

    public String getNestedPath() {
        return this.nestedPath;
    }

    public void pushNestedPath(String subPath) {
        this.nestedPathStack.push(getNestedPath());
        doSetNestedPath(getNestedPath() + subPath);
    }

    public void popNestedPath() throws IllegalArgumentException {
        try {
            String formerNestedPath = this.nestedPathStack.pop();
            doSetNestedPath(formerNestedPath);
        }
        catch (EmptyStackException ex) {
            throw new IllegalStateException("Cannot pop nested path: no nested path on stack");
        }
    }

    protected void doSetNestedPath(String nestedPath) {
        if (nestedPath == null) {
            nestedPath = "";
        }
        nestedPath = canonicalFieldName(nestedPath);
        if (nestedPath.length() > 0 && !nestedPath.endsWith(Errors.NESTED_PATH_SEPARATOR)) {
            nestedPath += Errors.NESTED_PATH_SEPARATOR;
        }
        this.nestedPath = nestedPath;
    }

    protected String fixedField(String field) {
        if (StringUtils.hasLength(field)) {
            return getNestedPath() + canonicalFieldName(field);
        }
        else {
            String path = getNestedPath();
            return (path.endsWith(Errors.NESTED_PATH_SEPARATOR) ?
                    path.substring(0, path.length() - NESTED_PATH_SEPARATOR.length()) : path);
        }
    }

    protected String canonicalFieldName(String field) {
        return field;
    }

    public void reject(String errorCode) {
        reject(errorCode, null, null);
    }

    public void reject(String errorCode, String defaultMessage) {
        reject(errorCode, null, defaultMessage);
    }

    public void rejectValue(String field, String errorCode) {
        rejectValue(field, errorCode, null, null);
    }

    public void rejectValue(String field, String errorCode, String defaultMessage) {
        rejectValue(field, errorCode, null, defaultMessage);
    }


    public boolean hasErrors() {
        return !getAllErrors().isEmpty();
    }

    public int getErrorCount() {
        return getAllErrors().size();
    }

    public List<ObjectError> getAllErrors() {
        List<ObjectError> result = new LinkedList<>();
        result.addAll(getGlobalErrors());
        result.addAll(getFieldErrors());
        return Collections.unmodifiableList(result);
    }

    public boolean hasGlobalErrors() {
        return (getGlobalErrorCount() > 0);
    }

    public int getGlobalErrorCount() {
        return getGlobalErrors().size();
    }

    public ObjectError getGlobalError() {
        List<ObjectError> globalErrors = getGlobalErrors();
        return (!globalErrors.isEmpty() ? globalErrors.get(0) : null);
    }

    public boolean hasFieldErrors() {
        return (getFieldErrorCount() > 0);
    }

    public int getFieldErrorCount() {
        return getFieldErrors().size();
    }

    public FieldError getFieldError() {
        List<FieldError> fieldErrors = getFieldErrors();
        return (!fieldErrors.isEmpty() ? fieldErrors.get(0) : null);
    }

    public boolean hasFieldErrors(String field) {
        return (getFieldErrorCount(field) > 0);
    }

    public int getFieldErrorCount(String field) {
        return getFieldErrors(field).size();
    }

    public List<FieldError> getFieldErrors(String field) {
        List<FieldError> fieldErrors = getFieldErrors();
        List<FieldError> result = new LinkedList<>();
        String fixedField = fixedField(field);
        for (FieldError error : fieldErrors) {
            if (isMatchingFieldError(fixedField, error)) {
                result.add(error);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public FieldError getFieldError(String field) {
        List<FieldError> fieldErrors = getFieldErrors(field);
        return (!fieldErrors.isEmpty() ? fieldErrors.get(0) : null);
    }

    public Class<?> getFieldType(String field) {
        Object value = getFieldValue(field);
        return (value != null ? value.getClass() : null);
    }

    protected boolean isMatchingFieldError(String field, FieldError fieldError) {
        if (field.equals(fieldError.getField())) {
            return true;
        }
        // Optimization: use charAt and regionMatches instead of endsWith and startsWith (SPR-11304)
        int endIndex = field.length() - 1;
        return (endIndex >= 0 && field.charAt(endIndex) == '*' &&
                (endIndex == 0 || field.regionMatches(0, fieldError.getField(), 0, endIndex)));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append(": ").append(getErrorCount()).append(" errors");
        for (ObjectError error : getAllErrors()) {
            sb.append('\n').append(error);
        }
        return sb.toString();
    }

}
