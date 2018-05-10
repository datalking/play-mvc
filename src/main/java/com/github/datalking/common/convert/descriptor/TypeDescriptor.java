package com.github.datalking.common.convert.descriptor;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于类型转换的类型描述符
 *
 * @author yaoo on 5/10/18
 */
public class TypeDescriptor implements Serializable {

    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private static final Map<Class<?>, TypeDescriptor> typeDescriptorCache = new HashMap<>(18);

    static {
        typeDescriptorCache.put(boolean.class, new TypeDescriptor(boolean.class));
        typeDescriptorCache.put(Boolean.class, new TypeDescriptor(Boolean.class));
        typeDescriptorCache.put(byte.class, new TypeDescriptor(byte.class));
        typeDescriptorCache.put(Byte.class, new TypeDescriptor(Byte.class));
        typeDescriptorCache.put(char.class, new TypeDescriptor(char.class));
        typeDescriptorCache.put(Character.class, new TypeDescriptor(Character.class));
        typeDescriptorCache.put(double.class, new TypeDescriptor(double.class));
        typeDescriptorCache.put(Double.class, new TypeDescriptor(Double.class));
        typeDescriptorCache.put(int.class, new TypeDescriptor(int.class));
        typeDescriptorCache.put(Integer.class, new TypeDescriptor(Integer.class));
        typeDescriptorCache.put(long.class, new TypeDescriptor(long.class));
        typeDescriptorCache.put(Long.class, new TypeDescriptor(Long.class));
        typeDescriptorCache.put(float.class, new TypeDescriptor(float.class));
        typeDescriptorCache.put(Float.class, new TypeDescriptor(Float.class));
        typeDescriptorCache.put(short.class, new TypeDescriptor(short.class));
        typeDescriptorCache.put(Short.class, new TypeDescriptor(Short.class));
        typeDescriptorCache.put(String.class, new TypeDescriptor(String.class));
        typeDescriptorCache.put(Object.class, new TypeDescriptor(Object.class));
    }

    private Class<?> type;

    private TypeDescriptor elementTypeDescriptor;

    private TypeDescriptor mapKeyTypeDescriptor;

    private TypeDescriptor mapValueTypeDescriptor;

    private Annotation[] annotations;

    public TypeDescriptor(MethodParameter methodParameter) {
        this(new ParameterDescriptor(methodParameter));
    }

    public TypeDescriptor(Field field) {
        this(new FieldDescriptor(field));
    }

    public TypeDescriptor(Property property) {
        this(new BeanPropertyDescriptor(property));
    }

    public static TypeDescriptor valueOf(Class<?> type) {
        if (type == null) {
            type = Object.class;
        }
        TypeDescriptor desc = typeDescriptorCache.get(type);
        return (desc != null ? desc : new TypeDescriptor(type));
    }

    public static TypeDescriptor collection(Class<?> collectionType, TypeDescriptor elementTypeDescriptor) {
        if (!Collection.class.isAssignableFrom(collectionType)) {
            throw new IllegalArgumentException("collectionType must be a java.util.Collection");
        }
        return new TypeDescriptor(collectionType, elementTypeDescriptor);
    }

    public static TypeDescriptor map(Class<?> mapType, TypeDescriptor keyTypeDescriptor, TypeDescriptor valueTypeDescriptor) {
        if (!Map.class.isAssignableFrom(mapType)) {
            throw new IllegalArgumentException("mapType must be a java.util.Map");
        }
        return new TypeDescriptor(mapType, keyTypeDescriptor, valueTypeDescriptor);
    }

    public static TypeDescriptor array(TypeDescriptor elementTypeDescriptor) {
        if (elementTypeDescriptor == null) {
            return null;
        }
        Class<?> type = Array.newInstance(elementTypeDescriptor.getType(), 0).getClass();
        return new TypeDescriptor(type,
                elementTypeDescriptor,
                null,
                null,
                elementTypeDescriptor.getAnnotations());
    }

    public static TypeDescriptor nested(MethodParameter methodParameter, int nestingLevel) {
        if (methodParameter.getNestingLevel() != 1) {
            throw new IllegalArgumentException("methodParameter nesting level must be 1: " +
                    "use the nestingLevel parameter");
        }
        return nested(new ParameterDescriptor(methodParameter), nestingLevel);
    }

    public static TypeDescriptor nested(Field field, int nestingLevel) {
        return nested(new FieldDescriptor(field), nestingLevel);
    }

    public static TypeDescriptor nested(Property property, int nestingLevel) {
        return nested(new BeanPropertyDescriptor(property), nestingLevel);
    }

    public static TypeDescriptor forObject(Object source) {
        return (source != null ? valueOf(source.getClass()) : null);
    }


    public Class<?> getType() {
        return this.type;
    }

    public Class<?> getObjectType() {
        return ClassUtils.resolvePrimitiveIfNecessary(getType());
    }

    public TypeDescriptor narrow(Object value) {
        if (value == null) {
            return this;
        }
        return new TypeDescriptor(value.getClass(), this.elementTypeDescriptor,
                this.mapKeyTypeDescriptor, this.mapValueTypeDescriptor, this.annotations);
    }

    public TypeDescriptor upcast(Class<?> superType) {
        if (superType == null) {
            return null;
        }
        Assert.isAssignable(superType, getType());
        return new TypeDescriptor(superType,
                this.elementTypeDescriptor,
                this.mapKeyTypeDescriptor,
                this.mapValueTypeDescriptor,
                this.annotations);
    }

    public String getName() {
        return ClassUtils.getQualifiedName(getType());
    }

    public boolean isPrimitive() {
        return getType().isPrimitive();
    }

    public Annotation[] getAnnotations() {
        return this.annotations;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : this.annotations) {
            if (annotation.annotationType().equals(annotationType)) {
                return (T) annotation;
            }
        }
        for (Annotation metaAnn : this.annotations) {
            T ann = metaAnn.annotationType().getAnnotation(annotationType);
            if (ann != null) {
                return ann;
            }
        }
        return null;
    }

    public boolean isAssignableTo(TypeDescriptor typeDescriptor) {
        boolean typesAssignable = typeDescriptor.getObjectType().isAssignableFrom(getObjectType());
        if (!typesAssignable) {
            return false;
        }
        if (isArray() && typeDescriptor.isArray()) {
            return getElementTypeDescriptor().isAssignableTo(typeDescriptor.getElementTypeDescriptor());
        } else if (isCollection() && typeDescriptor.isCollection()) {
            return isNestedAssignable(getElementTypeDescriptor(), typeDescriptor.getElementTypeDescriptor());
        } else if (isMap() && typeDescriptor.isMap()) {
            return isNestedAssignable(getMapKeyTypeDescriptor(), typeDescriptor.getMapKeyTypeDescriptor()) &&
                    isNestedAssignable(getMapValueTypeDescriptor(), typeDescriptor.getMapValueTypeDescriptor());
        } else {
            return true;
        }
    }


    // indexable type descriptor operations

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(getType());
    }

    public boolean isArray() {
        return getType().isArray();
    }

    public TypeDescriptor getElementTypeDescriptor() {
        assertCollectionOrArray();
        return this.elementTypeDescriptor;
    }

    public TypeDescriptor elementTypeDescriptor(Object element) {
        return narrow(element, getElementTypeDescriptor());
    }


    // map type descriptor operations

    public boolean isMap() {
        return Map.class.isAssignableFrom(getType());
    }

    public TypeDescriptor getMapKeyTypeDescriptor() {
        assertMap();
        return this.mapKeyTypeDescriptor;
    }

    public TypeDescriptor getMapKeyTypeDescriptor(Object mapKey) {
        return narrow(mapKey, getMapKeyTypeDescriptor());
    }

    public TypeDescriptor getMapValueTypeDescriptor() {
        assertMap();
        return this.mapValueTypeDescriptor;
    }

    public TypeDescriptor getMapValueTypeDescriptor(Object mapValue) {
        return narrow(mapValue, getMapValueTypeDescriptor());
    }


    // deprecations in Spring 3.1

    @Deprecated
    public Class<?> getElementType() {
        return getElementTypeDescriptor().getType();
    }

    @Deprecated
    public Class<?> getMapKeyType() {
        return getMapKeyTypeDescriptor().getType();
    }

    @Deprecated
    public Class<?> getMapValueType() {
        return getMapValueTypeDescriptor().getType();
    }


    // internal constructors

    private TypeDescriptor(Class<?> type) {
        this(new ClassDescriptor(type));
    }

    private TypeDescriptor(Class<?> collectionType, TypeDescriptor elementTypeDescriptor) {
        this(collectionType, elementTypeDescriptor, null, null, EMPTY_ANNOTATION_ARRAY);
    }

    private TypeDescriptor(Class<?> mapType, TypeDescriptor keyTypeDescriptor, TypeDescriptor valueTypeDescriptor) {
        this(mapType, null, keyTypeDescriptor, valueTypeDescriptor, EMPTY_ANNOTATION_ARRAY);
    }

    private TypeDescriptor(Class<?> type, TypeDescriptor elementTypeDescriptor, TypeDescriptor mapKeyTypeDescriptor,
                           TypeDescriptor mapValueTypeDescriptor, Annotation[] annotations) {

        this.type = type;
        this.elementTypeDescriptor = elementTypeDescriptor;
        this.mapKeyTypeDescriptor = mapKeyTypeDescriptor;
        this.mapValueTypeDescriptor = mapValueTypeDescriptor;
        this.annotations = annotations;
    }

    TypeDescriptor(AbstractDescriptor descriptor) {
        this.type = descriptor.getType();
        this.elementTypeDescriptor = descriptor.getElementTypeDescriptor();
        this.mapKeyTypeDescriptor = descriptor.getMapKeyTypeDescriptor();
        this.mapValueTypeDescriptor = descriptor.getMapValueTypeDescriptor();
        this.annotations = descriptor.getAnnotations();
    }


    // internal helpers

    static Annotation[] nullSafeAnnotations(Annotation[] annotations) {
        return (annotations != null ? annotations : EMPTY_ANNOTATION_ARRAY);
    }

    private static TypeDescriptor nested(AbstractDescriptor descriptor, int nestingLevel) {
        for (int i = 0; i < nestingLevel; i++) {
            descriptor = descriptor.nested();
            if (descriptor == null) {
                return null;
            }
        }
        return new TypeDescriptor(descriptor);
    }

    private void assertCollectionOrArray() {
        if (!isCollection() && !isArray()) {
            throw new IllegalStateException("Not a java.util.Collection or Array");
        }
    }

    private void assertMap() {
        if (!isMap()) {
            throw new IllegalStateException("Not a java.util.Map");
        }
    }

    private TypeDescriptor narrow(Object value, TypeDescriptor typeDescriptor) {
        if (typeDescriptor != null) {
            return typeDescriptor.narrow(value);
        } else {
            return (value != null ?
                    new TypeDescriptor(value.getClass(),
                            null,
                            null,
                            null,
                            this.annotations) :
                    null);
        }
    }

    private boolean isNestedAssignable(TypeDescriptor nestedTypeDescriptor, TypeDescriptor otherNestedTypeDescriptor) {
        if (nestedTypeDescriptor == null || otherNestedTypeDescriptor == null) {
            return true;
        }
        return nestedTypeDescriptor.isAssignableTo(otherNestedTypeDescriptor);
    }

    private String wildcard(TypeDescriptor typeDescriptor) {
        return (typeDescriptor != null ? typeDescriptor.toString() : "?");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TypeDescriptor)) {
            return false;
        }
        TypeDescriptor other = (TypeDescriptor) obj;
        if (!ObjectUtils.nullSafeEquals(this.type, other.type)) {
            return false;
        }
        if (this.annotations.length != other.annotations.length) {
            return false;
        }
        for (Annotation ann : this.annotations) {
            if (other.getAnnotation(ann.annotationType()) == null) {
                return false;
            }
        }
        if (isCollection() || isArray()) {
            return ObjectUtils.nullSafeEquals(this.elementTypeDescriptor, other.elementTypeDescriptor);
        } else if (isMap()) {
            return ObjectUtils.nullSafeEquals(this.mapKeyTypeDescriptor, other.mapKeyTypeDescriptor) &&
                    ObjectUtils.nullSafeEquals(this.mapValueTypeDescriptor, other.mapValueTypeDescriptor);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Annotation ann : this.annotations) {
            builder.append("@").append(ann.annotationType().getName()).append(' ');
        }
        builder.append(ClassUtils.getQualifiedName(getType()));
        if (isMap()) {
            builder.append("<").append(wildcard(this.mapKeyTypeDescriptor));
            builder.append(", ").append(wildcard(this.mapValueTypeDescriptor)).append(">");
        } else if (isCollection()) {
            builder.append("<").append(wildcard(this.elementTypeDescriptor)).append(">");
        }
        return builder.toString();
    }

}
