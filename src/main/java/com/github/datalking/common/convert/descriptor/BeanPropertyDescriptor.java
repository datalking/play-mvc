package com.github.datalking.common.convert.descriptor;

import com.github.datalking.common.GenericCollectionTypeResolver;
import com.github.datalking.common.MethodParameter;

import java.lang.annotation.Annotation;

/**
 * @author yaoo on 5/10/18
 */
public class BeanPropertyDescriptor extends AbstractDescriptor {

    private final Property property;

    private final MethodParameter methodParameter;

    private final Annotation[] annotations;

    public BeanPropertyDescriptor(Property property) {
        super(property.getType());
        this.property = property;
        this.methodParameter = property.getMethodParameter();
        this.annotations = property.getAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        return this.annotations;
    }

    @Override
    protected Class<?> resolveCollectionElementType() {
        return GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter);
    }

    @Override
    protected Class<?> resolveMapKeyType() {
        return GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter);
    }

    @Override
    protected Class<?> resolveMapValueType() {
        return GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter);
    }

    @Override
    protected AbstractDescriptor nested(Class<?> type, int typeIndex) {
        MethodParameter methodParameter = new MethodParameter(this.methodParameter);
        methodParameter.increaseNestingLevel();
        methodParameter.setTypeIndexForCurrentLevel(typeIndex);
        return new BeanPropertyDescriptor(type, this.property, methodParameter, this.annotations);
    }


    private BeanPropertyDescriptor(Class<?> type,
                                   Property propertyDescriptor,
                                   MethodParameter methodParameter,
                                   Annotation[] annotations) {
        super(type);
        this.property = propertyDescriptor;
        this.methodParameter = methodParameter;
        this.annotations = annotations;
    }

}
