package com.github.datalking.common.env;

import com.github.datalking.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yaoo on 5/28/18
 */
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public EnumerablePropertySource(String name, T source) {
        super(name, source);
    }

    protected EnumerablePropertySource(String name) {
        super(name);
    }

    public boolean containsProperty(String name) {
        Assert.notNull(name, "Property name must not be null");
        for (String candidate : getPropertyNames()) {
            if (candidate.equals(name)) {

                return true;
            }
        }

        return false;
    }

    public abstract String[] getPropertyNames();

}
