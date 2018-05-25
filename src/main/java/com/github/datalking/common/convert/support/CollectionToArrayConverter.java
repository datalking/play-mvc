package com.github.datalking.common.convert.support;

import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.common.convert.ConversionUtils;
import com.github.datalking.common.convert.converter.ConditionalGenericConverter;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 */
public final class CollectionToArrayConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;

    public CollectionToArrayConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, Object[].class));
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ConversionUtils.canConvertElements(
                sourceType.getElementTypeDescriptor(),
                targetType.getElementTypeDescriptor(),
                this.conversionService);
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Collection<?> sourceCollection = (Collection<?>) source;
        Object array = Array.newInstance(targetType.getElementTypeDescriptor().getType(), sourceCollection.size());
        int i = 0;
        for (Object sourceElement : sourceCollection) {

            Object targetElement = this.conversionService.convert(
                    sourceElement,
                    sourceType.elementTypeDescriptor(sourceElement),
                    targetType.getElementTypeDescriptor());

            Array.set(array, i++, targetElement);
        }
        return array;
    }

}
