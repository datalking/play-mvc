package com.github.datalking.common.env;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PropertySource抽象类
 *
 * @author yaoo on 5/28/18
 */
public abstract class PropertySource<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // 资源名，可以是本地properties文件名
    protected final String name;

    // 读取properties文件的键值对，作为map存储到这里
    protected final T source;

    public PropertySource(String name, T source) {
        Assert.hasText(name, "Property source name must contain at least one character");
        Assert.notNull(source, "Property source must not be null");
        this.name = name;
        this.source = source;
    }

    public PropertySource(String name) {
        this(name, (T) new Object());
    }

    public String getName() {
        return this.name;
    }

    public T getSource() {
        return this.source;
    }

    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    public abstract Object getProperty(String name);

    @Override
    public boolean equals(Object obj) {
        return (this == obj
                || (obj instanceof PropertySource
                && ObjectUtils.nullSafeEquals(this.name, ((PropertySource<?>) obj).name)));
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.name);
    }

    @Override
    public String toString() {
        if (logger.isDebugEnabled()) {
            return String.format("%s@%s [name='%s', properties=%s]", getClass().getSimpleName(), System.identityHashCode(this), this.name, this.source);
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
     * StubPropertySource用作占位符
     * <p>
     * PropertySource to be used as a placeholder in cases where an actual creation time.
     * For example, a {@code ServletContext}-based property source must wait until
     * the {@code ServletContext} object is available to its enclosing ApplicationContext.
     * In such cases, a stub should be used to hold the intended default position/order of the property source,
     * then be replaced during context refresh.
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
