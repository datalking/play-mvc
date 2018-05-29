package com.github.datalking.beans.factory.config;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;

/**
 * BeanDefinition的名称和target type的容器
 *
 * @author yaoo on 5/27/18
 */
//public class TypedStringValue implements BeanMetadataElement {
public class TypedStringValue {

    private String value;

    private volatile Object targetType;

    private Object source;

    private String specifiedTypeName;

    private volatile boolean dynamic;

    public TypedStringValue(String value) {
        setValue(value);
    }

    public TypedStringValue(String value, Class<?> targetType) {
        setValue(value);
        setTargetType(targetType);
    }

    public TypedStringValue(String value, String targetTypeName) {
        setValue(value);
        setTargetTypeName(targetTypeName);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setTargetType(Class<?> targetType) {
        Assert.notNull(targetType, "'targetType' must not be null");
        this.targetType = targetType;
    }

    public Class<?> getTargetType() {
        Object targetTypeValue = this.targetType;
        if (!(targetTypeValue instanceof Class)) {
            throw new IllegalStateException("Typed String value does not carry a resolved target type");
        }
        return (Class) targetTypeValue;
    }

    public void setTargetTypeName(String targetTypeName) {
        Assert.notNull(targetTypeName, "'targetTypeName' must not be null");
        this.targetType = targetTypeName;
    }

    public String getTargetTypeName() {
        Object targetTypeValue = this.targetType;
        if (targetTypeValue instanceof Class) {
            return ((Class) targetTypeValue).getName();
        } else {
            return (String) targetTypeValue;
        }
    }

    public boolean hasTargetType() {
        return (this.targetType instanceof Class);
    }

    /**
     * Determine the type to convert to, resolving it from a specified class name
     * if necessary. Will also reload a specified Class from its name when called
     * with the target type already resolved.
     *
     * @param classLoader the ClassLoader to use for resolving a (potential) class name
     * @return the resolved type to convert to
     * @throws ClassNotFoundException if the type cannot be resolved
     */
    public Class<?> resolveTargetType(ClassLoader classLoader) throws ClassNotFoundException {
        if (this.targetType == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(getTargetTypeName(), classLoader);
        this.targetType = resolvedClass;
        return resolvedClass;
    }


    /**
     * Set the configuration source {@code Object} for this metadata element.
     * <p>The exact type of the object will depend on the configuration mechanism used.
     */
    public void setSource(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return this.source;
    }

    /**
     * Set the type name as actually specified for this particular value, if any.
     */
    public void setSpecifiedTypeName(String specifiedTypeName) {
        this.specifiedTypeName = specifiedTypeName;
    }

    /**
     * Return the type name as actually specified for this particular value, if any.
     */
    public String getSpecifiedTypeName() {
        return this.specifiedTypeName;
    }

    /**
     * Mark this value as dynamic, i.e. as containing an expression
     * and hence not being subject to caching.
     */
    public void setDynamic() {
        this.dynamic = true;
    }

    /**
     * Return whether this value has been marked as dynamic.
     */
    public boolean isDynamic() {
        return this.dynamic;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TypedStringValue)) {
            return false;
        }
        TypedStringValue otherValue = (TypedStringValue) other;
        return (ObjectUtils.nullSafeEquals(this.value, otherValue.value) &&
                ObjectUtils.nullSafeEquals(this.targetType, otherValue.targetType));
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.targetType);
    }

    @Override
    public String toString() {
        return "TypedStringValue: value [" + this.value + "], target type [" + this.targetType + "]";
    }

}
