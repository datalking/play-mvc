package com.github.datalking.common.convert;

/**
 * 数据类型转换 接口
 *
 * @author yaoo on 5/4/18
 */
public interface ConversionService {

    // 判断是否可以将一个Java类转换为另一个Java类
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    // 将源类型对象转换为目标类型对象
    <T> T convert(Object source, Class<T> targetType);

//    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
//    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

}
