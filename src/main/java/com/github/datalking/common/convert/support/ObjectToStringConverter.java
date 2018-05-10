package com.github.datalking.common.convert.support;


import com.github.datalking.common.convert.converter.Converter;

/**
 */
public final class ObjectToStringConverter implements Converter<Object, String> {

	public String convert(Object source) {
		return source.toString();
	}

}
