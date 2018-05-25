package com.github.datalking.common.convert.support;

import com.github.datalking.common.convert.converter.ConditionalGenericConverter;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 */
public final class ObjectToObjectConverter implements ConditionalGenericConverter {

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object.class, Object.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.getType().equals(targetType.getType())) {
			// no conversion required
			return false;
		}
		return hasValueOfMethodOrConstructor(targetType.getType(), sourceType.getType());
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		Class<?> sourceClass = sourceType.getType();
		Class<?> targetClass = targetType.getType();
		Method method = getValueOfMethodOn(targetClass, sourceClass);
		try {
			if (method != null) {
				ReflectionUtils.makeAccessible(method);
				return method.invoke(null, source);
			}
			else {
				Constructor<?> constructor = getConstructor(targetClass, sourceClass);
				if (constructor != null) {
					ReflectionUtils.makeAccessible(constructor);
					return constructor.newInstance(source);
				}
			}
		}
		catch (InvocationTargetException ex) {
			ex.printStackTrace();
//			throw new ConversionFailedException(sourceType, targetType, source, ex.getTargetException());
		}
		catch (Throwable ex) {
			ex.printStackTrace();
//			throw new ConversionFailedException(sourceType, targetType, source, ex);
		}
		throw new IllegalStateException("No static valueOf(" + sourceClass.getName() +
				") method or Constructor(" + sourceClass.getName() + ") exists on " + targetClass.getName());
	}

	static boolean hasValueOfMethodOrConstructor(Class<?> clazz, Class<?> sourceParameterType) {
		return getValueOfMethodOn(clazz, sourceParameterType) != null || getConstructor(clazz, sourceParameterType) != null;
	}

	private static Method getValueOfMethodOn(Class<?> clazz, Class<?> sourceParameterType) {
		return ClassUtils.getStaticMethod(clazz, "valueOf", sourceParameterType);
	}

	private static Constructor<?> getConstructor(Class<?> clazz, Class<?> sourceParameterType) {
		return ClassUtils.getConstructorIfAvailable(clazz, sourceParameterType);
	}

}
