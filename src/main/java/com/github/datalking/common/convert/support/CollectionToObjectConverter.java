package com.github.datalking.common.convert.support;

import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.common.convert.ConversionUtils;
import com.github.datalking.common.convert.converter.ConditionalGenericConverter;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 */
public final class CollectionToObjectConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public CollectionToObjectConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Collection.class, Object.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(sourceType.getElementTypeDescriptor(), targetType, this.conversionService);
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		if (sourceType.isAssignableTo(targetType)) {
			return source;
		}
		Collection<?> sourceCollection = (Collection<?>) source;
		if (sourceCollection.size() == 0) {
			return null;
		}
		Object firstElement = sourceCollection.iterator().next();
		return this.conversionService.convert(firstElement, sourceType.elementTypeDescriptor(firstElement), targetType);
	}

}
