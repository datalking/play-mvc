package com.github.datalking.common.convert;

import com.github.datalking.common.convert.converter.GenericConverter;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;

/**
 * 类型转换工具类
 *
 * @author yaoo on 5/10/18
 */
public abstract class ConversionUtils {

    public static Object invokeConverter(GenericConverter converter,
                                         Object source,
                                         TypeDescriptor sourceType,
                                         TypeDescriptor targetType) {
        try {

            return converter.convert(source, sourceType, targetType);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static boolean canConvertElements(TypeDescriptor sourceElementType,
                                             TypeDescriptor targetElementType,
                                             ConversionService conversionService) {
        if (targetElementType == null) {
            // yes
            return true;
        }

        if (sourceElementType == null) {
            // maybe
            return true;
        }

        if (conversionService.canConvert(sourceElementType, targetElementType)) {
            // yes
            return true;
        } else if (sourceElementType.getType().isAssignableFrom(targetElementType.getType())) {
            // maybe;
            return true;
        } else {
            // no;
            return false;
        }
    }

}

