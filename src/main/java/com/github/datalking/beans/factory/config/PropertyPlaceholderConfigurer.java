package com.github.datalking.beans.factory.config;

import com.github.datalking.common.Constants;
import com.github.datalking.common.StringValueResolver;
import com.github.datalking.io.PropertyPlaceholderHelper;

import java.util.Properties;
import java.util.Set;

/**
 * 配置文件属性占位符解析
 * 旧版本的实现
 * deprecated in favor of {@link com.github.datalking.common.env.PropertySourcesPlaceholderConfigurer}
 * <p>
 * todo remove this class
 *
 * @author yaoo on 5/29/18
 */
@Deprecated
public class PropertyPlaceholderConfigurer extends PlaceholderConfigurerSupport {

    /**
     * Never check system properties.
     */
    public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

    /**
     * Check system properties if not resolvable in the specified properties.
     * This is the default.
     */
    public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

    /**
     * Check system properties first, before trying the specified properties.
     * This allows system properties to override any other property source.
     */
    public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;


    private static final Constants constants = new Constants(PropertyPlaceholderConfigurer.class);

    private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;

    //    private boolean searchSystemEnvironment = !SpringProperties.getFlag(AbstractEnvironment.IGNORE_GETENV_PROPERTY_NAME);
    private boolean searchSystemEnvironment = false;


    /**
     * Set the system property mode by the name of the corresponding constant,
     * e.g. "SYSTEM_PROPERTIES_MODE_OVERRIDE".
     *
     * @param constantName name of the constant
     * @throws java.lang.IllegalArgumentException if an invalid constant was specified
     * @see #setSystemPropertiesMode
     */
    public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
        this.systemPropertiesMode = constants.asNumber(constantName).intValue();
    }

    /**
     * Set how to check system properties: as fallback, as override, or never.
     * For example, will resolve ${user.dir} to the "user.dir" system property.
     * <p>The default is "fallback": If not being able to resolve a placeholder
     * with the specified properties, a system property will be tried.
     * "override" will check for a system property first, before trying the
     * specified properties. "never" will not check system properties at all.
     */
    public void setSystemPropertiesMode(int systemPropertiesMode) {
        this.systemPropertiesMode = systemPropertiesMode;
    }

    /**
     * Set whether to search for a matching system environment variable
     * if no matching system property has been found. Only applied when
     * "systemPropertyMode" is active (i.e. "fallback" or "override"), right
     * after checking JVM system properties.
     * <p>Default is "true". Switch this setting off to never resolve placeholders
     * against system environment variables. Note that it is generally recommended
     * to pass external values in as JVM system properties: This can easily be
     * achieved in a startup script, even for existing environment variables.
     * <p><b>NOTE:</b> Access to environment variables does not work on the
     * Sun VM 1.4, where the corresponding {@link System#getenv} support was
     * disabled - before it eventually got re-enabled for the Sun VM 1.5.
     * Please upgrade to 1.5 (or higher) if you intend to rely on the
     * environment variable support.
     *
     * @see #setSystemPropertiesMode
     * @see java.lang.System#getProperty(String)
     * @see java.lang.System#getenv(String)
     */
    public void setSearchSystemEnvironment(boolean searchSystemEnvironment) {
        this.searchSystemEnvironment = searchSystemEnvironment;
    }

    /**
     * Resolve the given placeholder using the given properties, performing
     * a system properties check according to the given mode.
     * <p>The default implementation delegates to {@code resolvePlaceholder
     * (placeholder, props)} before/after the system properties check.
     * <p>Subclasses can override this for custom resolution strategies,
     * including customized points for the system properties check.
     *
     * @param placeholder          the placeholder to resolve
     * @param props                the merged properties of this configurer
     * @param systemPropertiesMode the system properties mode,
     *                             according to the constants in this class
     * @return the resolved value, of null if none
     * @see #setSystemPropertiesMode
     * @see System#getProperty
     * @see #resolvePlaceholder(String, java.util.Properties)
     */
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        String propVal = null;
        if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
            propVal = resolveSystemProperty(placeholder);
        }
        if (propVal == null) {
            propVal = resolvePlaceholder(placeholder, props);
        }
        if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
            propVal = resolveSystemProperty(placeholder);
        }
        return propVal;
    }

    /**
     * Resolve the given placeholder using the given properties.
     * The default implementation simply checks for a corresponding property key.
     * <p>Subclasses can override this for customized placeholder-to-key mappings
     * or custom resolution strategies, possibly just using the given properties
     * as fallback.
     * <p>Note that system properties will still be checked before respectively
     * after this method is invoked, according to the system properties mode.
     *
     * @param placeholder the placeholder to resolve
     * @param props       the merged properties of this configurer
     * @return the resolved value, of {@code null} if none
     * @see #setSystemPropertiesMode
     */
    protected String resolvePlaceholder(String placeholder, Properties props) {
        return props.getProperty(placeholder);
    }

    /**
     * Resolve the given key as JVM system property, and optionally also as
     * system environment variable if no matching system property has been found.
     *
     * @param key the placeholder to resolve as system property key
     * @return the system property value, or {@code null} if not found
     * @see #setSearchSystemEnvironment
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    protected String resolveSystemProperty(String key) {
        try {
            String value = System.getProperty(key);
            if (value == null && this.searchSystemEnvironment) {
                value = System.getenv(key);
            }
            return value;
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not access system property '" + key + "': " + ex);
            }
            return null;
        }
    }


    /**
     * Visit each bean definition in the given bean factory and attempt to replace ${...} property
     * placeholders with values from the given properties.
     */
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) {

        StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);
        doProcessProperties(beanFactoryToProcess, valueResolver);
    }

    /**
     * Parse the given String value for placeholder resolution.
     *
     * @param strVal              the String value to parse
     * @param props               the Properties to resolve placeholders against
     * @param visitedPlaceholders the placeholders that have already been visited
     *                            during the current resolution attempt (ignored in this version of the code)
     * @deprecated as of Spring 3.0, in favor of using {@link #resolvePlaceholder}
     * with PropertyPlaceholderHelper.
     * Only retained for compatibility with Spring 2.5 extensions.
     */
    @Deprecated
    protected String parseStringValue(String strVal, Properties props, Set<?> visitedPlaceholders) {
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(
                placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
        PropertyPlaceholderHelper.PlaceholderResolver resolver = new PropertyPlaceholderConfigurerResolver(props);
        return helper.replacePlaceholders(strVal, resolver);
    }


    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final PropertyPlaceholderHelper helper;

        private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

        public PlaceholderResolvingStringValueResolver(Properties props) {
            this.helper = new PropertyPlaceholderHelper(
                    placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
            this.resolver = new PropertyPlaceholderConfigurerResolver(props);
        }

        public String resolveStringValue(String strVal) {
            String value = this.helper.replacePlaceholders(strVal, this.resolver);
            return (value.equals(nullValue) ? null : value);
        }
    }


    private class PropertyPlaceholderConfigurerResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private final Properties props;

        private PropertyPlaceholderConfigurerResolver(Properties props) {
            this.props = props;
        }

        public String resolvePlaceholder(String placeholderName) {
            return PropertyPlaceholderConfigurer.this.resolvePlaceholder(placeholderName, props, systemPropertiesMode);
        }
    }

}
