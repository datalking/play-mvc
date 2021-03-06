package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.common.GenericCollectionTypeResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.util.Assert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 依赖项的描述对象
 * 如A依赖B，则表示B的描述信息
 * 封装构造方法参数、普通方法参数或成员字段
 *
 * @author yaoo on 5/28/18
 */
public class DependencyDescriptor implements Serializable {

    private transient MethodParameter methodParameter;

    // 字段对象，一般是B的对象
    private transient Field field;

    // 字段名，一般是B的名称
    private String fieldName;

    // 外层类class对象，一般是A
    private Class<?> declaringClass;

    private String methodName;

    private Class[] parameterTypes;

    private int parameterIndex;

    private final boolean required;

    private final boolean eager;

    private int nestingLevel = 1;

    private transient Annotation[] fieldAnnotations;

    public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
        this(methodParameter, required, true);
    }

    public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
        Assert.notNull(methodParameter, "MethodParameter must not be null");
        this.methodParameter = methodParameter;
        this.declaringClass = methodParameter.getDeclaringClass();
        if (this.methodParameter.getMethod() != null) {
            this.methodName = methodParameter.getMethod().getName();
            this.parameterTypes = methodParameter.getMethod().getParameterTypes();
        } else {
            this.parameterTypes = methodParameter.getConstructor().getParameterTypes();
        }
        this.parameterIndex = methodParameter.getParameterIndex();
        this.required = required;
        this.eager = eager;
    }

    public DependencyDescriptor(Field field, boolean required) {
        this(field, required, true);
    }

    public DependencyDescriptor(Field field, boolean required, boolean eager) {
        Assert.notNull(field, "Field must not be null");
        this.field = field;
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
        this.required = required;
        this.eager = eager;
    }

    public DependencyDescriptor(DependencyDescriptor original) {
        this.methodParameter = (original.methodParameter != null ? new MethodParameter(original.methodParameter) : null);
        this.field = original.field;
        this.declaringClass = original.declaringClass;
        this.methodName = original.methodName;
        this.parameterTypes = original.parameterTypes;
        this.parameterIndex = original.parameterIndex;
        this.fieldName = original.fieldName;
        this.required = original.required;
        this.eager = original.eager;
        this.nestingLevel = original.nestingLevel;
        this.fieldAnnotations = original.fieldAnnotations;
    }


    /**
     * Return the wrapped MethodParameter, if any.
     * <p>Note: Either MethodParameter or Field is available.
     *
     * @return the MethodParameter, or {@code null} if none
     */
    public MethodParameter getMethodParameter() {
        return this.methodParameter;
    }

    /**
     * Return the wrapped Field, if any.
     * <p>Note: Either MethodParameter or Field is available.
     *
     * @return the Field, or {@code null} if none
     */
    public Field getField() {
        return this.field;
    }

    /**
     * Return whether this dependency is required.
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Return whether this dependency is 'eager' in the sense of
     * eagerly resolving potential target beans for type matching.
     */
    public boolean isEager() {
        return this.eager;
    }


    /**
     * Increase this descriptor's nesting level.
     *
     * @see MethodParameter#increaseNestingLevel()
     */
    public void increaseNestingLevel() {
        this.nestingLevel++;
        if (this.methodParameter != null) {
            this.methodParameter.increaseNestingLevel();
        }
    }

    /**
     * Initialize parameter name discovery for the underlying method parameter, if any.
     * <p>This method does not actually try to retrieve the parameter name at
     * this point; it just allows discovery to happen when the application calls
     * {@link #getDependencyName()} (if ever).
     */
    public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
        if (this.methodParameter != null) {
            this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
        }
    }

    /**
     * Determine the name of the wrapped parameter/field.
     *
     * @return the declared name (never {@code null})
     */
    public String getDependencyName() {
        return (this.field != null ? this.field.getName() : this.methodParameter.getParameterName());
    }

    /**
     * Determine the declared (non-generic) type of the wrapped parameter/field.
     *
     * @return the declared type (never {@code null})
     */
    public Class<?> getDependencyType() {
        if (this.field != null) {
            if (this.nestingLevel > 1) {
                Type type = this.field.getGenericType();
                if (type instanceof ParameterizedType) {
                    Type arg = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (arg instanceof Class) {
                        return (Class) arg;
                    } else if (arg instanceof ParameterizedType) {
                        arg = ((ParameterizedType) arg).getRawType();
                        if (arg instanceof Class) {
                            return (Class) arg;
                        }
                    }
                }
                return Object.class;
            } else {
                return this.field.getType();
            }
        } else {
            return this.methodParameter.getNestedParameterType();
        }
    }

    /**
     * Determine the generic element type of the wrapped Collection parameter/field, if any.
     *
     * @return the generic type, or {@code null} if none
     */
    public Class<?> getCollectionType() {
        return (this.field != null ?
                GenericCollectionTypeResolver.getCollectionFieldType(this.field, this.nestingLevel) :
                GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter));
    }

    /**
     * Determine the generic key type of the wrapped Map parameter/field, if any.
     *
     * @return the generic type, or {@code null} if none
     */
    public Class<?> getMapKeyType() {
        return (this.field != null ?
                GenericCollectionTypeResolver.getMapKeyFieldType(this.field, this.nestingLevel) :
                GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter));
    }

    /**
     * Determine the generic value type of the wrapped Map parameter/field, if any.
     *
     * @return the generic type, or {@code null} if none
     */
    public Class<?> getMapValueType() {
        return (this.field != null ?
                GenericCollectionTypeResolver.getMapValueFieldType(this.field, this.nestingLevel) :
                GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter));
    }

    /**
     * Obtain the annotations associated with the wrapped parameter/field, if any.
     */
    public Annotation[] getAnnotations() {
        if (this.field != null) {
            if (this.fieldAnnotations == null) {
                this.fieldAnnotations = this.field.getAnnotations();
            }
            return this.fieldAnnotations;
        } else {
            return this.methodParameter.getParameterAnnotations();
        }
    }


    //---------------------------------------------------------------------
    // Serialization support
    //---------------------------------------------------------------------

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Rely on default serialization; just initialize state after deserialization.
        ois.defaultReadObject();

        // Restore reflective handles (which are unfortunately not serializable)
        try {
            if (this.fieldName != null) {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            } else {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                } else {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }
                for (int i = 1; i < this.nestingLevel; i++) {
                    this.methodParameter.increaseNestingLevel();
                }
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Could not find original class structure", ex);
        }
    }

    public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {

        return beanFactory.getBean(beanName);
    }


    @Override
    public String toString() {
        return (this.field != null ? "field '" + this.field.getName() + "'" : this.methodParameter.toString());
    }

}
