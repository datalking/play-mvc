package com.github.datalking.common.convert.editor;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 */
public class CustomCollectionEditor extends PropertyEditorSupport {

	private final Class collectionType;

	private final boolean nullAsEmptyCollection;

	public CustomCollectionEditor(Class collectionType) {
		this(collectionType, false);
	}

	public CustomCollectionEditor(Class collectionType, boolean nullAsEmptyCollection) {
		if (collectionType == null) {
			throw new IllegalArgumentException("Collection type is required");
		}
		if (!Collection.class.isAssignableFrom(collectionType)) {
			throw new IllegalArgumentException(
					"Collection type [" + collectionType.getName() + "] does not implement [java.util.Collection]");
		}
		this.collectionType = collectionType;
		this.nullAsEmptyCollection = nullAsEmptyCollection;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(text);
	}

	@Override
	public void setValue(Object value) {
		if (value == null && this.nullAsEmptyCollection) {
			super.setValue(createCollection(this.collectionType, 0));
		}
		else if (value == null || (this.collectionType.isInstance(value) && !alwaysCreateNewCollection())) {
			// Use the source value as-is, as it matches the target type.
			super.setValue(value);
		}
		else if (value instanceof Collection) {
			// Convert Collection elements.
			Collection source = (Collection) value;
			Collection target = createCollection(this.collectionType, source.size());
			for (Object elem : source) {
				target.add(convertElement(elem));
			}
			super.setValue(target);
		}
		else if (value.getClass().isArray()) {
			// Convert array elements to Collection elements.
			int length = Array.getLength(value);
			Collection target = createCollection(this.collectionType, length);
			for (int i = 0; i < length; i++) {
				target.add(convertElement(Array.get(value, i)));
			}
			super.setValue(target);
		}
		else {
			// A plain value: convert it to a Collection with a single element.
			Collection target = createCollection(this.collectionType, 1);
			target.add(convertElement(value));
			super.setValue(target);
		}
	}

	protected Collection createCollection(Class collectionType, int initialCapacity) {
		if (!collectionType.isInterface()) {
			try {
				return (Collection) collectionType.newInstance();
			}
			catch (Exception ex) {
				throw new IllegalArgumentException(
						"Could not instantiate collection class [" + collectionType.getName() + "]: " + ex.getMessage());
			}
		}
		else if (List.class.equals(collectionType)) {
			return new ArrayList(initialCapacity);
		}
		else if (SortedSet.class.equals(collectionType)) {
			return new TreeSet();
		}
		else {
			return new LinkedHashSet(initialCapacity);
		}
	}

	protected boolean alwaysCreateNewCollection() {
		return false;
	}

	protected Object convertElement(Object element) {
		return element;
	}

	@Override
	public String getAsText() {
		return null;
	}

}
