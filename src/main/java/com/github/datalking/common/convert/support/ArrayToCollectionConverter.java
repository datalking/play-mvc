package com.github.datalking.common.convert.support;

import com.github.datalking.common.CollectionFactory;
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
public final class ArrayToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public ArrayToCollectionConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object[].class, Collection.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(
				sourceType.getElementTypeDescriptor(), targetType.getElementTypeDescriptor(), this.conversionService);
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}

		int length = Array.getLength(source);
		TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
		Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), length);

		if (elementDesc == null) {
			for (int i = 0; i < length; i++) {
				Object sourceElement = Array.get(source, i);
				target.add(sourceElement);
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				Object sourceElement = Array.get(source, i);
				Object targetElement = this.conversionService.convert(sourceElement,
						sourceType.elementTypeDescriptor(sourceElement), elementDesc);
				target.add(targetElement);
			}
		}
		return target;
	}

}
