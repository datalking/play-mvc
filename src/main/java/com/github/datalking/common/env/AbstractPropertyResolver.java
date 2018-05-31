package com.github.datalking.common.env;

import com.github.datalking.common.ConfigurableConversionService;
import com.github.datalking.common.convert.DefaultConversionService;
import com.github.datalking.io.PropertyPlaceholderHelper;
import com.github.datalking.util.SystemPropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author yaoo on 5/29/18
 */
public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ConfigurableConversionService conversionService = new DefaultConversionService();

    private PropertyPlaceholderHelper nonStrictHelper;

    private PropertyPlaceholderHelper strictHelper;

    private boolean ignoreUnresolvableNestedPlaceholders = false;

    private String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

    private String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    private String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;

    private final Set<String> requiredProperties = new LinkedHashSet<>();


    public ConfigurableConversionService getConversionService() {
        return this.conversionService;
    }

    public void setConversionService(ConfigurableConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Set the prefix that placeholders replaced by this resolver must begin with.
     * <p>The default is "${".
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * Set the suffix that placeholders replaced by this resolver must end with.
     * <p>The default is "}".
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Specify the separating character between the placeholders replaced by this
     * resolver and their associated default value, or {@code null} if no such
     * special character should be processed as a value separator.
     * <p>The default is ":".
     */
    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    /**
     * Set whether to throw an exception when encountering an unresolvable placeholder nested within the value of a given property.
     * A {@code false} value indicates strict resolution, i.e. that an exception will be thrown.
     * A {@code true} value indicates that unresolvable nested placeholders should be passed through in their unresolved
     * ${...} form.
     * <p>The default is {@code false}.
     */
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        this.ignoreUnresolvableNestedPlaceholders = ignoreUnresolvableNestedPlaceholders;
    }

    public void setRequiredProperties(String... requiredProperties) {
        for (String key : requiredProperties) {
            this.requiredProperties.add(key);
        }
    }

    public void validateRequiredProperties() {
//        MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
//        for (String key : this.requiredProperties) {
//            if (this.getProperty(key) == null) {
//                ex.addMissingRequiredProperty(key);
//            }
//        }
//        if (!ex.getMissingRequiredProperties().isEmpty()) {
//            throw ex;
//        }
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null ? value : defaultValue);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return (value != null ? value : defaultValue);
    }

    public String getRequiredProperty(String key) throws IllegalStateException {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException(String.format("required key [%s] not found", key));
        }
        return value;
    }

    public <T> T getRequiredProperty(String key, Class<T> valueType) throws IllegalStateException {
        T value = getProperty(key, valueType);
        if (value == null) {
            throw new IllegalStateException(String.format("required key [%s] not found", key));
        }
        return value;
    }

    public String resolvePlaceholders(String text) {
        if (this.nonStrictHelper == null) {
            this.nonStrictHelper = createPlaceholderHelper(true);
        }

        return doResolvePlaceholders(text, this.nonStrictHelper);
    }

    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {

        if (this.strictHelper == null) {
            // 创建占位符解析器
            this.strictHelper = createPlaceholderHelper(false);
        }

        // 负责解析路径中或者名称中含有占位符的字串
        return doResolvePlaceholders(text, this.strictHelper);
    }

    protected String resolveNestedPlaceholders(String value) {
        return (this.ignoreUnresolvableNestedPlaceholders ?
                resolvePlaceholders(value) : resolveRequiredPlaceholders(value));
    }

    private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {

        return new PropertyPlaceholderHelper(
                this.placeholderPrefix,
                this.placeholderSuffix,
                this.valueSeparator,
                ignoreUnresolvablePlaceholders);
    }

    /**
     * 负责解析路径中或者名字中含有占位符的字串，并负责填充上具体的值
     */
    private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {

        return helper.replacePlaceholders(text, new PropertyPlaceholderHelper.PlaceholderResolver() {

            @Override
            public String resolvePlaceholder(String placeholderName) {
                return getPropertyAsRawString(placeholderName);
            }
        });
    }


    /**
     * Retrieve the specified property as a raw String, i.e. without resolution of nested placeholders.
     *
     * @param key the property name to resolve
     * @return the property value or {@code null} if none found
     */
    protected abstract String getPropertyAsRawString(String key);

}
