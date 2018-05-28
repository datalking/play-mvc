package com.github.datalking.common.env;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yaoo on 5/28/18
 */
public abstract class PropertySource<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final String name;

    protected final T source;

    public PropertySource(String name, T source) {
        Assert.hasText(name, "Property source name must contain at least one character");
        Assert.notNull(source, "Property source must not be null");
        this.name = name;
        this.source = source;
    }

    /**
     * Create a new {@code PropertySource} with the given name and with a new {@code Object}
     * instance as the underlying source.
     * <p>Often useful in testing scenarios when creating anonymous implementations that
     * never query an actual source but rather return hard-coded values.
     */
    @SuppressWarnings("unchecked")
    public PropertySource(String name) {
        this(name, (T) new Object());
    }


    /**
     * Return the name of this {@code PropertySource}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the underlying source object for this {@code PropertySource}.
     */
    public T getSource() {
        return this.source;
    }

    /**
     * Return whether this {@code PropertySource} contains the given name.
     * <p>This implementation simply checks for a {@code null} return value
     * from {@link #getProperty(String)}. Subclasses may wish to implement
     * a more efficient algorithm if possible.
     *
     * @param name the property name to find
     */
    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    /**
     * Return the value associated with the given name,
     * or {@code null} if not found.
     *
     * @param name the property to find
     * @see PropertyResolver#getRequiredProperty(String)
     */
    public abstract Object getProperty(String name);


    /**
     * This {@code PropertySource} object is equal to the given object if:
     * <ul>
     * <li>they are the same instance
     * <li>the {@code name} properties for both objects are equal
     * </ul>
     * <p>No properties other than {@code name} are evaluated.
     */
    @Override
    public boolean equals(Object obj) {
        return (this == obj || (obj instanceof PropertySource &&
                ObjectUtils.nullSafeEquals(this.name, ((PropertySource<?>) obj).name)));
    }

    /**
     * Return a hash code derived from the {@code name} property
     * of this {@code PropertySource} object.
     */
    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.name);
    }

    /**
     * Produce concise output (type and name) if the current log level does not include
     * debug. If debug is enabled, produce verbose output including the hash code of the
     * PropertySource instance and every name/value property pair.
     * <p>This variable verbosity is useful as a property source such as system properties
     * or environment variables may contain an arbitrary number of property pairs,
     * potentially leading to difficult to read exception and log messages.
     */
    @Override
    public String toString() {
        if (logger.isDebugEnabled()) {
            return String.format("%s@%s [name='%s', properties=%s]",
                    getClass().getSimpleName(), System.identityHashCode(this), this.name, this.source);
        } else {
            return String.format("%s [name='%s']", getClass().getSimpleName(), this.name);
        }
    }


    /**
     * Return a {@code PropertySource} implementation intended for collection comparison purposes only.
     * <p>Primarily for internal use, but given a collection of {@code PropertySource} objects, may be
     * used as follows:
     * <pre class="code">
     * {@code List<PropertySource<?>> sources = new ArrayList<PropertySource<?>>();
     * sources.add(new MapPropertySource("sourceA", mapA));
     * sources.add(new MapPropertySource("sourceB", mapB));
     * assert sources.contains(PropertySource.named("sourceA"));
     * assert sources.contains(PropertySource.named("sourceB"));
     * assert !sources.contains(PropertySource.named("sourceC"));
     * }</pre>
     * The returned {@code PropertySource} will throw {@code UnsupportedOperationException}
     * if any methods other than {@code equals(Object)}, {@code hashCode()}, and {@code toString()}
     * are called.
     *
     * @param name the name of the comparison {@code PropertySource} to be created and returned.
     */
    public static PropertySource<?> named(String name) {
        return new ComparisonPropertySource(name);
    }


    /**
     * StubPropertySource占位符
     * {@code PropertySource} to be used as a placeholder in cases where an actual
     * property source cannot be eagerly initialized at application context
     * creation time.  For example, a {@code ServletContext}-based property source
     * must wait until the {@code ServletContext} object is available to its enclosing
     * {@code ApplicationContext}.  In such cases, a stub should be used to hold the
     * intended default position/order of the property source, then be replaced
     * during context refresh.
     */
    public static class StubPropertySource extends PropertySource<Object> {

        public StubPropertySource(String name) {
            super(name, new Object());
        }

        @Override
        public String getProperty(String name) {
            return null;
        }
    }

    static class ComparisonPropertySource extends StubPropertySource {

        private static final String USAGE_ERROR = "ComparisonPropertySource instances are for use with collection comparison only";

        public ComparisonPropertySource(String name) {
            super(name);
        }

        @Override
        public Object getSource() {
            throw new UnsupportedOperationException(USAGE_ERROR);
        }

        @Override
        public boolean containsProperty(String name) {
            throw new UnsupportedOperationException(USAGE_ERROR);
        }

        @Override
        public String getProperty(String name) {
            throw new UnsupportedOperationException(USAGE_ERROR);
        }

        @Override
        public String toString() {
            return String.format("%s [name='%s']", getClass().getSimpleName(), this.name);
        }
    }

}
