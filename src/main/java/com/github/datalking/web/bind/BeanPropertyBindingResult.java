package com.github.datalking.web.bind;

import com.github.datalking.beans.BeanWrapper;
import com.github.datalking.beans.ConfigurablePropertyAccessor;
import com.github.datalking.beans.PropertyAccessorFactory;
import com.github.datalking.util.Assert;

import java.io.Serializable;

/**
 * @author yaoo on 5/10/18
 */
public class BeanPropertyBindingResult extends AbstractPropertyBindingResult implements Serializable {

    private final Object target;

    private final boolean autoGrowNestedPaths;

    private final int autoGrowCollectionLimit;

    private transient BeanWrapper beanWrapper;

    public BeanPropertyBindingResult(Object target, String objectName) {
        this(target, objectName, true, Integer.MAX_VALUE);
    }

    public BeanPropertyBindingResult(Object target, String objectName, boolean autoGrowNestedPaths, int autoGrowCollectionLimit) {
        super(objectName);
        this.target = target;
        this.autoGrowNestedPaths = autoGrowNestedPaths;
        this.autoGrowCollectionLimit = autoGrowCollectionLimit;
    }


    @Override
    public final Object getTarget() {
        return this.target;
    }

    @Override
    public final ConfigurablePropertyAccessor getPropertyAccessor() {
        if (this.beanWrapper == null) {
            this.beanWrapper = createBeanWrapper();
//            this.beanWrapper.setExtractOldValueForEditor(true);
//            this.beanWrapper.setAutoGrowNestedPaths(this.autoGrowNestedPaths);
//            this.beanWrapper.setAutoGrowCollectionLimit(this.autoGrowCollectionLimit);
        }
        return this.beanWrapper;
    }

    protected BeanWrapper createBeanWrapper() {
        Assert.state(this.target != null, "Cannot access properties on null bean instance '" + getObjectName() + "'!");
        return PropertyAccessorFactory.forBeanPropertyAccess(this.target);
    }

}
