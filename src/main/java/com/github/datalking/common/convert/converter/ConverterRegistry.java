package com.github.datalking.common.convert.converter;

/**
 * @author yaoo on 5/10/18
 */
public interface ConverterRegistry {


    void addConverter(Converter<?, ?> converter);

    void addConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter);

    void addConverter(GenericConverter converter);

    void addConverterFactory(ConverterFactory<?, ?> factory);

    void removeConvertible(Class<?> sourceType, Class<?> targetType);

}
