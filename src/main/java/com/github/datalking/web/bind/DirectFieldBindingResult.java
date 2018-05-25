package com.github.datalking.web.bind;

import com.github.datalking.beans.ConfigurablePropertyAccessor;
import com.github.datalking.beans.PropertyAccessorFactory;
import com.github.datalking.util.Assert;

/**
 * @author yaoo on 5/10/18
 */
public class DirectFieldBindingResult extends AbstractPropertyBindingResult {

    private final Object target;

    private transient ConfigurablePropertyAccessor directFieldAccessor;

    public DirectFieldBindingResult(Object target, String objectName) {
        super(objectName);
        this.target = target;
    }

    @Override
    public final Object getTarget() {
        return this.target;
    }

    @Override
    public final ConfigurablePropertyAccessor getPropertyAccessor() {
        if (this.directFieldAccessor == null) {
            this.directFieldAccessor = createDirectFieldAccessor();
            this.directFieldAccessor.setExtractOldValueForEditor(true);
        }
        return this.directFieldAccessor;
    }

    protected ConfigurablePropertyAccessor createDirectFieldAccessor() {
        Assert.state(this.target != null, "Cannot access fields on null '" + getObjectName() + "'!");
        return PropertyAccessorFactory.forDirectFieldAccess(this.target);
    }

}
