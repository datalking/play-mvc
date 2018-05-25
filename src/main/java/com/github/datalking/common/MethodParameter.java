package com.github.datalking.common;

import com.github.datalking.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * 方法参数操作工具类
 */
public class MethodParameter {

    // 普通方法实例
    private final Method method;

    // 构造方法实例
    private final Constructor<?> constructor;

    // 方法参数索引，从0开始，-1表示返回值类型，0是第一个参数
    private final int parameterIndex;

    // 参数具体类型
    private volatile Class<?> parameterType;

    // 参数类型泛型
    private volatile Type genericParameterType;

    // 方法参数上的注解，如@PathVariable
    private volatile Annotation[] parameterAnnotations;

    // 获取方法参数名的类
    private volatile ParameterNameDiscoverer parameterNameDiscoverer;

    // 方法参数名
    private volatile String parameterName;

    private int nestingLevel = 1;

    Map<Integer, Integer> typeIndexesPerLevel;

    Map<TypeVariable, Type> typeVariableMap;

    public MethodParameter(Method method, int parameterIndex) {
        this(method, parameterIndex, 1);
    }

    public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
        Assert.notNull(method, "Method must not be null");
        this.method = method;
        this.parameterIndex = parameterIndex;
        this.nestingLevel = nestingLevel;
        this.constructor = null;
    }

    public MethodParameter(Constructor<?> constructor, int parameterIndex) {
        this(constructor, parameterIndex, 1);
    }

    public MethodParameter(Constructor<?> constructor, int parameterIndex, int nestingLevel) {
        Assert.notNull(constructor, "Constructor must not be null");
        this.constructor = constructor;
        this.parameterIndex = parameterIndex;
        this.nestingLevel = nestingLevel;
        this.method = null;
    }

    public MethodParameter(MethodParameter original) {
        Assert.notNull(original, "Original must not be null");
        this.method = original.method;
        this.constructor = original.constructor;
        this.parameterIndex = original.parameterIndex;
        this.parameterType = original.parameterType;
        this.genericParameterType = original.genericParameterType;
        this.parameterAnnotations = original.parameterAnnotations;
        this.parameterNameDiscoverer = original.parameterNameDiscoverer;
        this.parameterName = original.parameterName;
        this.nestingLevel = original.nestingLevel;
        this.typeIndexesPerLevel = original.typeIndexesPerLevel;
        this.typeVariableMap = original.typeVariableMap;
    }

    public static MethodParameter forMethodOrConstructor(Object methodOrConstructor, int parameterIndex) {

        if (methodOrConstructor instanceof Method) {
            return new MethodParameter((Method) methodOrConstructor, parameterIndex);

        } else if (methodOrConstructor instanceof Constructor) {
            return new MethodParameter((Constructor<?>) methodOrConstructor, parameterIndex);

        } else {
            throw new IllegalArgumentException(methodOrConstructor + "is neither a Method nor a Constructor");
        }
    }

    public Method getMethod() {
        return this.method;
    }

    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    public Class<?> getDeclaringClass() {
        return getMember().getDeclaringClass();
    }

    private Member getMember() {
        return (this.method != null ? this.method : this.constructor);
    }

    private AnnotatedElement getAnnotatedElement() {
        return (this.method != null ? this.method : this.constructor);
    }

    public int getParameterIndex() {
        return this.parameterIndex;
    }

    void setParameterType(Class<?> parameterType) {
        this.parameterType = parameterType;
    }

    public Class<?> getParameterType() {
        if (this.parameterType == null) {
            if (this.parameterIndex < 0) {
                this.parameterType = (this.method != null ? this.method.getReturnType() : null);
            } else {
                this.parameterType = (this.method != null ?
                        this.method.getParameterTypes()[this.parameterIndex] :
                        this.constructor.getParameterTypes()[this.parameterIndex]);
            }
        }
        return this.parameterType;
    }

    public Type getGenericParameterType() {
        if (this.genericParameterType == null) {
            if (this.parameterIndex < 0) {
                this.genericParameterType = (this.method != null ? this.method.getGenericReturnType() : null);
            } else {
                this.genericParameterType = (this.method != null ?
                        this.method.getGenericParameterTypes()[this.parameterIndex] :
                        this.constructor.getGenericParameterTypes()[this.parameterIndex]);
            }
        }
        return this.genericParameterType;
    }

    public Class<?> getNestedParameterType() {
        if (this.nestingLevel > 1) {
            Type type = getGenericParameterType();
            if (type instanceof ParameterizedType) {
                Integer index = getTypeIndexForCurrentLevel();
                Type arg = ((ParameterizedType) type).getActualTypeArguments()[index != null ? index : 0];
                if (arg instanceof Class) {
                    return (Class<?>) arg;
                } else if (arg instanceof ParameterizedType) {
                    arg = ((ParameterizedType) arg).getRawType();
                    if (arg instanceof Class) {
                        return (Class<?>) arg;
                    }
                }
            }
            return Object.class;
        } else {
            return getParameterType();
        }
    }

    public Annotation[] getMethodAnnotations() {
        return getAnnotatedElement().getAnnotations();
    }

    public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
        return getAnnotatedElement().getAnnotation(annotationType);
    }

    public Annotation[] getParameterAnnotations() {
        if (this.parameterAnnotations == null) {
            Annotation[][] annotationArray = (this.method != null ?
                    this.method.getParameterAnnotations() : this.constructor.getParameterAnnotations());
            if (this.parameterIndex >= 0 && this.parameterIndex < annotationArray.length) {
                this.parameterAnnotations = annotationArray[this.parameterIndex];
            } else {
                this.parameterAnnotations = new Annotation[0];
            }
        }
        return this.parameterAnnotations;
    }

    public <T extends Annotation> T getParameterAnnotation(Class<T> annotationType) {
        Annotation[] anns = getParameterAnnotations();
        for (Annotation ann : anns) {
            if (annotationType.isInstance(ann)) {
                return (T) ann;
            }
        }
        return null;
    }

    public boolean hasParameterAnnotations() {
        return (getParameterAnnotations().length != 0);
    }

    public <T extends Annotation> boolean hasParameterAnnotation(Class<T> annotationType) {
        return (getParameterAnnotation(annotationType) != null);
    }

    // 设置 parameterNameDiscoverer
    public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    public String getParameterName() {
        ParameterNameDiscoverer discoverer = this.parameterNameDiscoverer;
        if (discoverer != null) {
            String[] parameterNames = (this.method != null ?
                    discoverer.getParameterNames(this.method) : discoverer.getParameterNames(this.constructor));
            if (parameterNames != null) {
                this.parameterName = parameterNames[this.parameterIndex];
            }
            this.parameterNameDiscoverer = null;
        }
        return this.parameterName;
    }

    public void increaseNestingLevel() {
        this.nestingLevel++;
    }

    public void decreaseNestingLevel() {
        getTypeIndexesPerLevel().remove(this.nestingLevel);
        this.nestingLevel--;
    }

    // 对于列表来来说，1代表内部列表，2代表内部列表的元素
    public int getNestingLevel() {
        return this.nestingLevel;
    }

    public void setTypeIndexForCurrentLevel(int typeIndex) {
        getTypeIndexesPerLevel().put(this.nestingLevel, typeIndex);
    }

    public Integer getTypeIndexForCurrentLevel() {
        return getTypeIndexForLevel(this.nestingLevel);
    }

    public Integer getTypeIndexForLevel(int nestingLevel) {
        return getTypeIndexesPerLevel().get(nestingLevel);
    }

    private Map<Integer, Integer> getTypeIndexesPerLevel() {
        if (this.typeIndexesPerLevel == null) {
            this.typeIndexesPerLevel = new HashMap<Integer, Integer>(4);
        }
        return this.typeIndexesPerLevel;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodParameter)) {
            return false;
        }
        MethodParameter otherParam = (MethodParameter) other;
        return (this.parameterIndex == otherParam.parameterIndex && getMember().equals(otherParam.getMember()));
    }

    @Override
    public int hashCode() {
        return (getMember().hashCode() * 31 + this.parameterIndex);
    }

}
