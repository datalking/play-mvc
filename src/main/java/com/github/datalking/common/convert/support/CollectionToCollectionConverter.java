package com.github.datalking.common.convert.support;

import com.github.datalking.common.CollectionFactory;
import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.common.convert.ConversionUtils;
import com.github.datalking.common.convert.converter.ConditionalGenericConverter;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 */
public final class CollectionToCollectionConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;

    public CollectionToCollectionConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, Collection.class));
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

        // Shortcut if possible...
        boolean copyRequired = !targetType.getType().isInstance(source);
        if (!copyRequired && sourceCollection.isEmpty()) {
            return source;
        }
        TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
        if (elementDesc == null && !copyRequired) {
            return source;
        }

        Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), sourceCollection.size());
        if (elementDesc == null) {
            target.addAll(sourceCollection);
        } else {
            for (Object sourceElement : sourceCollection) {
                Object targetElement = this.conversionService.convert(sourceElement,
                        sourceType.elementTypeDescriptor(sourceElement), elementDesc);
                target.add(targetElement);
                if (sourceElement != targetElement) {
                    copyRequired = true;
                }
            }
        }

        return (copyRequired ? target : source);
    }

}
