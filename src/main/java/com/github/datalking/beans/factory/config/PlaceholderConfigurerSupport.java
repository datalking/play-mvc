package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.BeanNameAware;
import com.github.datalking.common.StringValueResolver;

/**
 * 解析Bean Definition属性值占位符 抽象类
 */
public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer
        implements BeanNameAware, BeanFactoryAware {

    private static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    private static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    private static final String DEFAULT_VALUE_SEPARATOR = ":";


    /**
     * Defaults to {@value #DEFAULT_PLACEHOLDER_PREFIX}
     */
    protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    /**
     * Defaults to {@value #DEFAULT_PLACEHOLDER_SUFFIX}
     */
    protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    /**
     * Defaults to {@value #DEFAULT_VALUE_SEPARATOR}
     */
    protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;

    protected boolean ignoreUnresolvablePlaceholders = false;

    protected String nullValue;

    private BeanFactory beanFactory;

    private String beanName;


    /**
     * Set the prefix that a placeholder string starts with.
     * The default is {@value #DEFAULT_PLACEHOLDER_PREFIX}.
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * Set the suffix that a placeholder string ends with.
     * The default is {@value #DEFAULT_PLACEHOLDER_SUFFIX}.
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Specify the separating character between the placeholder variable
     * and the associated default value, or {@code null} if no such
     * special character should be processed as a value separator.
     * The default is {@value #DEFAULT_VALUE_SEPARATOR}.
     */
    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    /**
     * Set a value that should be treated as {@code null} when
     * resolved as a placeholder value: e.g. "" (empty String) or "null".
     * <p>Note that this will only apply to full property values,
     * not to parts of concatenated values.
     * <p>By default, no such null value is defined. This means that
     * there is no way to express {@code null} as a property
     * value unless you explicitly map a corresponding value here.
     */
    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * Set whether to ignore unresolvable placeholders.
     * <p>Default is "false": An exception will be thrown if a placeholder fails
     * to resolve. Switch this flag to "true" in order to preserve the placeholder
     * String as-is in such a case, leaving it up to other placeholder configurers
     * to resolve it.
     */
    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    /**
     * Only necessary to check that we're not parsing our own bean definition,
     * to avoid failing on unresolvable placeholders in properties file locations.
     * The latter case can happen with placeholders for system properties in
     * resource locations.
     *
     * @see #setLocations
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Only necessary to check that we're not parsing our own bean definition,
     * to avoid failing on unresolvable placeholders in properties file locations.
     * The latter case can happen with placeholders for system properties in
     * resource locations.
     *
     * @see #setLocations
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       StringValueResolver valueResolver) {

        BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (String curName : beanNames) {
            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file locations.
            if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
                try {
                    visitor.visitBeanDefinition(bd);
                } catch (Exception ex) {
//                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        }

        // New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
//        beanFactoryToProcess.resolveAliases(valueResolver);

        // New in Spring 3.0: resolve placeholders in embedded values such as annotation attributes.
//        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
    }

}
