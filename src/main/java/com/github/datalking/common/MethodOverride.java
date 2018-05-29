package com.github.datalking.common;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;

import java.lang.reflect.Method;

/**
 * @author yaoo on 5/29/18
 */
//public abstract class MethodOverride implements BeanMetadataElement {
public abstract class MethodOverride {

    private final String methodName;

    private boolean overloaded = true;

    private Object source;

    protected MethodOverride(String methodName) {
        Assert.notNull(methodName, "Method name must not be null");
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    protected void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    protected boolean isOverloaded() {
        return this.overloaded;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return this.source;
    }

    public abstract boolean matches(Method method);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodOverride)) {
            return false;
        }
        MethodOverride that = (MethodOverride) other;
        return (ObjectUtils.nullSafeEquals(this.methodName, that.methodName) &&
                ObjectUtils.nullSafeEquals(this.source, that.source));
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(this.methodName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.source);
        return hashCode;
    }

}
