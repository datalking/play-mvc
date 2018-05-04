package com.github.datalking.common.convert;

/**
 * @author yaoo on 5/4/18
 */
public interface ConversionService {

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    <T> T convert(Object source, Class<T> targetType);

//    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
//    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

}
