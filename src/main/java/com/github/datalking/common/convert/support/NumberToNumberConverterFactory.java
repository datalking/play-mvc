package com.github.datalking.common.convert.support;

import com.github.datalking.common.convert.converter.ConditionalConverter;
import com.github.datalking.common.convert.converter.Converter;
import com.github.datalking.common.convert.converter.ConverterFactory;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.NumberUtils;

/**
 * @author yaoo on 5/10/18
 */
public final class NumberToNumberConverterFactory
        implements ConverterFactory<Number, Number>, ConditionalConverter {

    public <T extends Number> Converter<Number, T> getConverter(Class<T> targetType) {
        return new NumberToNumber<T>(targetType);
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return !sourceType.equals(targetType);
    }

    private final static class NumberToNumber<T extends Number> implements Converter<Number, T> {

        private final Class<T> targetType;

        public NumberToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        public T convert(Number source) {
            return NumberUtils.convertNumberToTargetClass(source, this.targetType);
        }
    }

}
