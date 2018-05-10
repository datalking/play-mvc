package com.github.datalking.common.convert.support;

import com.github.datalking.common.convert.converter.Converter;
import com.github.datalking.common.convert.converter.ConverterFactory;
import com.github.datalking.util.NumberUtils;

/**
 */
public final class StringToNumberConverterFactory implements ConverterFactory<String, Number> {

    public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {

        return new StringToNumber<>(targetType);
    }

    private static final class StringToNumber<T extends Number> implements Converter<String, T> {

        private final Class<T> targetType;

        public StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        public T convert(String source) {
            if (source.length() == 0) {
                return null;
            }

            return NumberUtils.parseNumber(source, this.targetType);
        }
    }

}
