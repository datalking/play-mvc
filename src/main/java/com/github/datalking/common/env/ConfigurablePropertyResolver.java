package com.github.datalking.common.env;

import com.github.datalking.common.ConfigurableConversionService;

/**
 * @author yaoo on 5/28/18
 */
public interface ConfigurablePropertyResolver extends PropertyResolver {

    ConfigurableConversionService getConversionService();

    void setConversionService(ConfigurableConversionService conversionService);

    void setPlaceholderPrefix(String placeholderPrefix);

    void setPlaceholderSuffix(String placeholderSuffix);

    void setValueSeparator(String valueSeparator);

    void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);

    void setRequiredProperties(String... requiredProperties);

    void validateRequiredProperties();

}

