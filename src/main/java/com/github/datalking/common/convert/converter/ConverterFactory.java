package com.github.datalking.common.convert.converter;

/**
 * @author yaoo on 5/10/18
 */
public interface ConverterFactory<S, R> {

    <T extends R> Converter<S, T> getConverter(Class<T> targetType);

}
