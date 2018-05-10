package com.github.datalking.common.convert.converter;

import com.github.datalking.common.convert.descriptor.TypeDescriptor;

/**
 * @author yaoo on 5/10/18
 */
public interface ConditionalConverter {

    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);

}
