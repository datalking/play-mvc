package com.github.datalking.common;

import com.github.datalking.util.Assert;

/**
 * @author yaoo on 5/10/18
 */
public class ObjectError extends DefaultMessageSourceResolvable {

    private final String objectName;

    public ObjectError(String objectName, String defaultMessage) {
        this(objectName, null, null, defaultMessage);
    }

    public ObjectError(String objectName, String[] codes, Object[] arguments, String defaultMessage) {
        super(codes, arguments, defaultMessage);
        Assert.notNull(objectName, "Object name must not be null");
        this.objectName = objectName;
    }

    public String getObjectName() {
        return this.objectName;
    }

    @Override
    public String toString() {
        return "Error in object '" + this.objectName + "': " + resolvableToString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(getClass().equals(other.getClass())) || !super.equals(other)) {
            return false;
        }
        ObjectError otherError = (ObjectError) other;
        return getObjectName().equals(otherError.getObjectName());
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 29 + getObjectName().hashCode();
    }

}

