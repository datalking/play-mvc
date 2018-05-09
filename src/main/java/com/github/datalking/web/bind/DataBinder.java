package com.github.datalking.web.bind;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.common.MethodParameter;

/**
 * @author yaoo on 4/29/18
 */
public class DataBinder {

    public static final String DEFAULT_OBJECT_NAME = "target";

    public static final int DEFAULT_AUTO_GROW_COLLECTION_LIMIT = 256;

    private final Object target;

    private final String objectName;

    public DataBinder(Object target) {
        this(target, DEFAULT_OBJECT_NAME);
    }

    public DataBinder(Object target, String objectName) {
        this.target = target;
        this.objectName = objectName;
    }

    public Object getTarget() {
        return this.target;
    }

    public String getObjectName() {
        return this.objectName;
    }

    protected void doBind(MutablePropertyValues mpvs) {
//        checkAllowedFields(mpvs);
//        checkRequiredFields(mpvs);
        applyPropertyValues(mpvs);
    }

    protected void applyPropertyValues(MutablePropertyValues mpvs) {

    }

    public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) {

        return getTypeConverter().convertIfNecessary(value, requiredType, methodParam);
    }


}
