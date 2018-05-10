package com.github.datalking.common.convert.descriptor;

import com.github.datalking.common.GenericCollectionTypeResolver;

import java.lang.annotation.Annotation;

/**
 * @author yaoo on 5/10/18
 */
public class ClassDescriptor extends AbstractDescriptor {

    ClassDescriptor(Class<?> type) {
        super(type);
    }

    @Override
    public Annotation[] getAnnotations() {
        return TypeDescriptor.EMPTY_ANNOTATION_ARRAY;
    }

    @Override
    protected Class<?> resolveCollectionElementType() {
        return GenericCollectionTypeResolver.getCollectionType((Class) getType());
    }

    @Override
    protected Class<?> resolveMapKeyType() {
        return GenericCollectionTypeResolver.getMapKeyType((Class) getType());
    }

    @Override
    protected Class<?> resolveMapValueType() {
        return GenericCollectionTypeResolver.getMapValueType((Class) getType());
    }

    @Override
    protected AbstractDescriptor nested(Class<?> type, int typeIndex) {
        return new ClassDescriptor(type);
    }

}
