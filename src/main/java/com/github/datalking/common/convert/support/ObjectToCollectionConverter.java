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
public final class ObjectToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public ObjectToCollectionConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object.class, Collection.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(
				sourceType,
				targetType.getElementTypeDescriptor(),
				this.conversionService);
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}

		TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
		Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), 1);

		if (elementDesc == null || elementDesc.isCollection()) {
			target.add(source);
		}
		else {
			Object singleElement = this.conversionService.convert(source, sourceType, elementDesc);
			target.add(singleElement);
		}
		return target;
	}

}
