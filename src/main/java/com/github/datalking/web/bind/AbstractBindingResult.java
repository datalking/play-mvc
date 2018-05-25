package com.github.datalking.web.bind;

import com.github.datalking.beans.PropertyEditorRegistry;
import com.github.datalking.common.AbstractErrors;
import com.github.datalking.common.BindingResult;
import com.github.datalking.common.DefaultMessageCodesResolver;
import com.github.datalking.common.Errors;
import com.github.datalking.common.FieldError;
import com.github.datalking.common.MessageCodesResolver;
import com.github.datalking.common.ObjectError;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yaoo on 5/10/18
 */
public abstract class AbstractBindingResult extends AbstractErrors implements BindingResult, Serializable {

    private final String objectName;

    private MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();

    private final List<ObjectError> errors = new LinkedList<>();

    private final Set<String> suppressedFields = new HashSet<>();

    protected AbstractBindingResult(String objectName) {
        this.objectName = objectName;
    }

    public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
        Assert.notNull(messageCodesResolver, "MessageCodesResolver must not be null");
        this.messageCodesResolver = messageCodesResolver;
    }

    public MessageCodesResolver getMessageCodesResolver() {
        return this.messageCodesResolver;
    }

    //---------------------------------------------------------------------
    // Implementation of the Errors interface
    //---------------------------------------------------------------------

    public String getObjectName() {
        return this.objectName;
    }


    public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
        addError(new ObjectError(getObjectName(), resolveMessageCodes(errorCode), errorArgs, defaultMessage));
    }

    public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
        if ("".equals(getNestedPath()) && !StringUtils.hasLength(field)) {
            // We're at the top of the nested object hierarchy,
            // so the present level is not a field but rather the top object.
            // The best we can do is register a global error here...
            reject(errorCode, errorArgs, defaultMessage);
            return;
        }
        String fixedField = fixedField(field);
        Object newVal = getActualFieldValue(fixedField);
        FieldError fe = new FieldError(
                getObjectName(), fixedField, newVal, false,
                resolveMessageCodes(errorCode, field), errorArgs, defaultMessage);
        addError(fe);
    }

    public void addError(ObjectError error) {
        this.errors.add(error);
    }

    public void addAllErrors(Errors errors) {
        if (!errors.getObjectName().equals(getObjectName())) {
            throw new IllegalArgumentException("Errors object needs to have same object name");
        }
        this.errors.addAll(errors.getAllErrors());
    }

    public String[] resolveMessageCodes(String errorCode) {
        return getMessageCodesResolver().resolveMessageCodes(errorCode, getObjectName());
    }

    public String[] resolveMessageCodes(String errorCode, String field) {
        Class<?> fieldType = getFieldType(field);
        return getMessageCodesResolver().resolveMessageCodes(
                errorCode, getObjectName(), fixedField(field), fieldType);
    }

    @Override
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    @Override
    public int getErrorCount() {
        return this.errors.size();
    }

    @Override
    public List<ObjectError> getAllErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    public List<ObjectError> getGlobalErrors() {
        List<ObjectError> result = new LinkedList<>();
        for (ObjectError objectError : this.errors) {
            if (!(objectError instanceof FieldError)) {
                result.add(objectError);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public ObjectError getGlobalError() {
        for (ObjectError objectError : this.errors) {
            if (!(objectError instanceof FieldError)) {
                return objectError;
            }
        }
        return null;
    }

    public List<FieldError> getFieldErrors() {
        List<FieldError> result = new LinkedList<>();
        for (ObjectError objectError : this.errors) {
            if (objectError instanceof FieldError) {
                result.add((FieldError) objectError);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public FieldError getFieldError() {
        for (ObjectError objectError : this.errors) {
            if (objectError instanceof FieldError) {
                return (FieldError) objectError;
            }
        }
        return null;
    }

    @Override
    public List<FieldError> getFieldErrors(String field) {
        List<FieldError> result = new LinkedList<>();
        String fixedField = fixedField(field);
        for (ObjectError objectError : this.errors) {
            if (objectError instanceof FieldError && isMatchingFieldError(fixedField, (FieldError) objectError)) {
                result.add((FieldError) objectError);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public FieldError getFieldError(String field) {
        String fixedField = fixedField(field);
        for (ObjectError objectError : this.errors) {
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                if (isMatchingFieldError(fixedField, fieldError)) {
                    return fieldError;
                }
            }
        }
        return null;
    }

    public Object getFieldValue(String field) {
        FieldError fieldError = getFieldError(field);
        // Use rejected value in case of error, current bean property value else.
        Object value = (fieldError != null ? fieldError.getRejectedValue() :
                getActualFieldValue(fixedField(field)));
        // Apply formatting, but not on binding failures like type mismatches.
        if (fieldError == null || !fieldError.isBindingFailure()) {
            value = formatFieldValue(field, value);
        }
        return value;
    }

    @Override
    public Class<?> getFieldType(String field) {
        Object value = getActualFieldValue(fixedField(field));
        if (value != null) {
            return value.getClass();
        }
        return null;
    }


    //---------------------------------------------------------------------
    // Implementation of BindingResult interface
    //---------------------------------------------------------------------

    public Map<String, Object> getModel() {
        Map<String, Object> model = new LinkedHashMap<>(2);
        model.put(getObjectName(), getTarget());
        // Errors instance, even if no errors.
        model.put(MODEL_KEY_PREFIX + getObjectName(), this);
        return model;
    }

    public Object getRawFieldValue(String field) {
        return getActualFieldValue(fixedField(field));
    }

    public PropertyEditor findEditor(String field, Class<?> valueType) {
        PropertyEditorRegistry editorRegistry = getPropertyEditorRegistry();
        if (editorRegistry != null) {
            Class<?> valueTypeToUse = valueType;
            if (valueTypeToUse == null) {
                valueTypeToUse = getFieldType(field);
            }
            return editorRegistry.findCustomEditor(valueTypeToUse, fixedField(field));
        }
        else {
            return null;
        }
    }

    public PropertyEditorRegistry getPropertyEditorRegistry() {
        return null;
    }

    public void recordSuppressedField(String field) {
        this.suppressedFields.add(field);
    }

    public String[] getSuppressedFields() {
        return StringUtils.toStringArray(this.suppressedFields);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BindingResult)) {
            return false;
        }
        BindingResult otherResult = (BindingResult) other;
        return (getObjectName().equals(otherResult.getObjectName()) &&
                ObjectUtils.nullSafeEquals(getTarget(), otherResult.getTarget()) &&
                getAllErrors().equals(otherResult.getAllErrors()));
    }

    @Override
    public int hashCode() {
        return getObjectName().hashCode();
    }

    public abstract Object getTarget();

    protected abstract Object getActualFieldValue(String field);

    protected Object formatFieldValue(String field, Object value) {
        return value;
    }

}
