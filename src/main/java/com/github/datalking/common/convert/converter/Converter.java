package com.github.datalking.common.convert.converter;

/**
 * @author yaoo on 5/10/18
 */
public interface Converter<S, T> {

    T convert(S source);

}
