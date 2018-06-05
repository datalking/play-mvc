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

    protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;

    protected boolean ignoreUnresolvablePlaceholders = false;

    protected String nullValue;

    private BeanFactory beanFactory;

    private String beanName;

    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 解析BeanDefinitionMap中所有的属性占位符
     */
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

//        System.out.println();
        // New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
//        beanFactoryToProcess.resolveAliases(valueResolver);

        // New in Spring 3.0: resolve placeholders in embedded values such as annotation attributes.
//        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
    }

}
