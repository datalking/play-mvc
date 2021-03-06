package com.github.datalking.common.convert.descriptor;

import com.github.datalking.common.GenericCollectionTypeResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoo on 5/10/18
 */
public class FieldDescriptor extends AbstractDescriptor {

    private final Field field;

    private final int nestingLevel;

    private Map<Integer, Integer> typeIndexesPerLevel;

    public FieldDescriptor(Field field) {
        super(field.getType());
        this.field = field;
        this.nestingLevel = 1;
    }

    private FieldDescriptor(Class<?> type, Field field, int nestingLevel, int typeIndex, Map<Integer, Integer> typeIndexesPerLevel) {
        super(type);
        this.field = field;
        this.nestingLevel = nestingLevel;
        this.typeIndexesPerLevel = typeIndexesPerLevel;
        this.typeIndexesPerLevel.put(nestingLevel, typeIndex);
    }

    @Override
    public Annotation[] getAnnotations() {
        return TypeDescriptor.nullSafeAnnotations(this.field.getAnnotations());
    }

    @Override
    protected Class<?> resolveCollectionElementType() {
        return GenericCollectionTypeResolver.getCollectionFieldType(this.field, this.nestingLevel, this.typeIndexesPerLevel);
    }

    @Override
    protected Class<?> resolveMapKeyType() {
        return GenericCollectionTypeResolver.getMapKeyFieldType(this.field, this.nestingLevel, this.typeIndexesPerLevel);
    }

    @Override
    protected Class<?> resolveMapValueType() {
        return GenericCollectionTypeResolver.getMapValueFieldType(this.field, this.nestingLevel, this.typeIndexesPerLevel);
    }

    @Override
    protected AbstractDescriptor nested(Class<?> type, int typeIndex) {
        if (this.typeIndexesPerLevel == null) {
            this.typeIndexesPerLevel = new HashMap<>(4);
        }
        return new FieldDescriptor(type, this.field, this.nestingLevel + 1, typeIndex, this.typeIndexesPerLevel);
    }

}
