package com.github.datalking.common.env;

import com.github.datalking.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yaoo on 5/28/18
 */
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

//    @Deprecated
//    protected static final String[] EMPTY_NAMES_ARRAY = new String[0];

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public EnumerablePropertySource(String name, T source) {
        super(name, source);
    }


    /**
     * Return whether this {@code PropertySource} contains a property with the given name.
     * <p>This implementation checks for the presence of the given name within the
     * {@link #getPropertyNames()} array.
     *
     * @param name the name of the property to find
     */
    public boolean containsProperty(String name) {
        Assert.notNull(name, "Property name must not be null");
        for (String candidate : getPropertyNames()) {
            if (candidate.equals(name)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("PropertySource [%s] contains '%s'", getName(), name));
                }
                return true;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(String.format("PropertySource [%s] does not contain '%s'", getName(), name));
        }
        return false;
    }

    /**
     * Return the names of all properties contained by the {@linkplain #getSource() source} object (never {@code null}).
     */
    public abstract String[] getPropertyNames();

}
