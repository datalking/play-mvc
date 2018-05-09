package com.github.datalking.common;

import com.github.datalking.util.Assert;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * 泛型工具类
 */
public abstract class GenericTypeResolver {

    private static final Map<Class, Map<TypeVariable, Type>> typeVariableCache = new ConcurrentReferenceHashMap<>();

    //
    public static Type getTargetType(MethodParameter methodParam) {
        Assert.notNull(methodParam, "MethodParameter must not be null");
        if (methodParam.getConstructor() != null) {
            return methodParam.getConstructor().getGenericParameterTypes()[methodParam.getParameterIndex()];
        } else {
            if (methodParam.getParameterIndex() >= 0) {
                return methodParam.getMethod().getGenericParameterTypes()[methodParam.getParameterIndex()];
            } else {
                return methodParam.getMethod().getGenericReturnType();
            }
        }
    }

    //
    public static Class<?> resolveParameterType(MethodParameter methodParam, Class<?> clazz) {
        //
        Type genericType = getTargetType(methodParam);
        Assert.notNull(clazz, "Class must not be null");

        //
        Map<TypeVariable, Type> typeVariableMap = getTypeVariableMap(clazz);
        //
        Type rawType = getRawType(genericType, typeVariableMap);
        Class<?> result = (rawType instanceof Class ? (Class) rawType : methodParam.getParameterType());

        methodParam.setParameterType(result);
        methodParam.typeVariableMap = typeVariableMap;

        return result;
    }

    public static Class<?> resolveReturnType(Method method, Class<?> clazz) {
        Assert.notNull(method, "Method must not be null");
        Type genericType = method.getGenericReturnType();
        Assert.notNull(clazz, "Class must not be null");
        Map<TypeVariable, Type> typeVariableMap = getTypeVariableMap(clazz);
        Type rawType = getRawType(genericType, typeVariableMap);
        return (rawType instanceof Class ? (Class<?>) rawType : method.getReturnType());
    }

    @Deprecated
    public static Class<?> resolveReturnTypeForGenericMethod(Method method, Object[] args) {
        Assert.notNull(method, "Method must not be null");
        Assert.notNull(args, "Argument array must not be null");

        TypeVariable<Method>[] declaredTypeVariables = method.getTypeParameters();
        Type genericReturnType = method.getGenericReturnType();
        Type[] methodArgumentTypes = method.getGenericParameterTypes();

        // No declared type variables to inspect, so just return the standard return type.
        if (declaredTypeVariables.length == 0) {
            return method.getReturnType();
        }

        // The supplied argument list is too short for the method's signature, so
        // return null, since such a method invocation would fail.
        if (args.length < methodArgumentTypes.length) {
            return null;
        }

        // Ensure that the type variable (e.g., T) is declared directly on the method
        // itself (e.g., via <T>), not on the enclosing class or interface.
        boolean locallyDeclaredTypeVariableMatchesReturnType = false;
        for (TypeVariable<Method> currentTypeVariable : declaredTypeVariables) {
            if (currentTypeVariable.equals(genericReturnType)) {
                locallyDeclaredTypeVariableMatchesReturnType = true;
                break;
            }
        }

        if (locallyDeclaredTypeVariableMatchesReturnType) {
            for (int i = 0; i < methodArgumentTypes.length; i++) {
                Type currentMethodArgumentType = methodArgumentTypes[i];
                if (currentMethodArgumentType.equals(genericReturnType)) {
                    return args[i].getClass();
                }
                if (currentMethodArgumentType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) currentMethodArgumentType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    for (Type typeArg : actualTypeArguments) {
                        if (typeArg.equals(genericReturnType)) {
                            if (args[i] instanceof Class) {
                                return (Class<?>) args[i];
                            } else {
                                // Consider adding logic to determine the class of the typeArg, if possible.
                                // For now, just fall back...
                                return method.getReturnType();
                            }
                        }
                    }
                }
            }
        }

        // Fall back...
        return method.getReturnType();
    }


    public static Class<?> resolveReturnTypeArgument(Method method, Class<?> genericIfc) {
        Assert.notNull(method, "method must not be null");
        Type returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        if (returnType.equals(genericIfc)) {
            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType targetType = (ParameterizedType) genericReturnType;
                Type[] actualTypeArguments = targetType.getActualTypeArguments();
                Type typeArg = actualTypeArguments[0];
                if (!(typeArg instanceof WildcardType)) {
                    return (Class<?>) typeArg;
                }
            } else {
                return null;
            }
        }
        return resolveTypeArgument((Class<?>) returnType, genericIfc);
    }

    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericIfc) {
        Class<?>[] typeArgs = resolveTypeArguments(clazz, genericIfc);
        if (typeArgs == null) {
            return null;
        }
        if (typeArgs.length != 1) {
            throw new IllegalArgumentException("Expected 1 type argument on generic interface [" +
                    genericIfc.getName() + "] but found " + typeArgs.length);
        }
        return typeArgs[0];
    }

    public static Class<?>[] resolveTypeArguments(Class<?> clazz, Class<?> genericIfc) {
        return doResolveTypeArguments(clazz, clazz, genericIfc);
    }

    private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Class<?> classToIntrospect, Class<?> genericIfc) {
        while (classToIntrospect != null) {
            if (genericIfc.isInterface()) {
                Type[] ifcs = classToIntrospect.getGenericInterfaces();
                for (Type ifc : ifcs) {
                    Class<?>[] result = doResolveTypeArguments(ownerClass, ifc, genericIfc);
                    if (result != null) {
                        return result;
                    }
                }
            } else {
                try {
                    Class<?>[] result = doResolveTypeArguments(ownerClass, classToIntrospect.getGenericSuperclass(), genericIfc);
                    if (result != null) {
                        return result;
                    }
                } catch (MalformedParameterizedTypeException ex) {
                    // from getGenericSuperclass() - return null to skip further superclass traversal
                    return null;
                }
            }
            classToIntrospect = classToIntrospect.getSuperclass();
        }
        return null;
    }

    private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Type ifc, Class<?> genericIfc) {
        if (ifc instanceof ParameterizedType) {
            ParameterizedType paramIfc = (ParameterizedType) ifc;
            Type rawType = paramIfc.getRawType();
            if (genericIfc.equals(rawType)) {
                Type[] typeArgs = paramIfc.getActualTypeArguments();
                Class<?>[] result = new Class[typeArgs.length];
                for (int i = 0; i < typeArgs.length; i++) {
                    Type arg = typeArgs[i];
                    result[i] = extractClass(ownerClass, arg);
                }
                return result;
            } else if (genericIfc.isAssignableFrom((Class) rawType)) {
                return doResolveTypeArguments(ownerClass, (Class) rawType, genericIfc);
            }
        } else if (ifc != null && genericIfc.isAssignableFrom((Class) ifc)) {
            return doResolveTypeArguments(ownerClass, (Class) ifc, genericIfc);
        }
        return null;
    }

    private static Class<?> extractClass(Class<?> ownerClass, Type arg) {
        if (arg instanceof ParameterizedType) {
            return extractClass(ownerClass, ((ParameterizedType) arg).getRawType());
        } else if (arg instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) arg;
            Type gt = gat.getGenericComponentType();
            Class<?> componentClass = extractClass(ownerClass, gt);
            return Array.newInstance(componentClass, 0).getClass();
        } else if (arg instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) arg;
            arg = getTypeVariableMap(ownerClass).get(tv);
            if (arg == null) {
                arg = extractBoundForTypeVariable(tv);
                if (arg instanceof ParameterizedType) {
                    return extractClass(ownerClass, ((ParameterizedType) arg).getRawType());
                }
            } else {
                return extractClass(ownerClass, arg);
            }
        }
        return (arg instanceof Class ? (Class) arg : Object.class);
    }

    public static Class<?> resolveType(Type genericType, Map<TypeVariable, Type> typeVariableMap) {
        Type resolvedType = getRawType(genericType, typeVariableMap);
        if (resolvedType instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) resolvedType).getGenericComponentType();
            Class<?> componentClass = resolveType(componentType, typeVariableMap);
            resolvedType = Array.newInstance(componentClass, 0).getClass();
        }
        return (resolvedType instanceof Class ? (Class) resolvedType : Object.class);
    }

    static Type getRawType(Type genericType, Map<TypeVariable, Type> typeVariableMap) {
        Type resolvedType = genericType;
        if (genericType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) genericType;
            resolvedType = typeVariableMap.get(tv);
            if (resolvedType == null) {
                resolvedType = extractBoundForTypeVariable(tv);
            }
        }
        if (resolvedType instanceof ParameterizedType) {
            return ((ParameterizedType) resolvedType).getRawType();
        } else {
            return resolvedType;
        }
    }

    public static Map<TypeVariable, Type> getTypeVariableMap(Class<?> clazz) {
        Map<TypeVariable, Type> typeVariableMap = typeVariableCache.get(clazz);

        if (typeVariableMap == null) {
            typeVariableMap = new HashMap<>();

            // interfaces
            extractTypeVariablesFromGenericInterfaces(clazz.getGenericInterfaces(), typeVariableMap);

            try {
                // super class
                Class<?> type = clazz;
                while (type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
                    Type genericType = type.getGenericSuperclass();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        populateTypeMapFromParameterizedType(pt, typeVariableMap);
                    }
                    extractTypeVariablesFromGenericInterfaces(type.getSuperclass().getGenericInterfaces(), typeVariableMap);
                    type = type.getSuperclass();
                }
            } catch (MalformedParameterizedTypeException ex) {
                // from getGenericSuperclass() - ignore and continue with member class check
            }

            try {
                // enclosing class
                Class<?> type = clazz;
                while (type.isMemberClass()) {
                    Type genericType = type.getGenericSuperclass();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        populateTypeMapFromParameterizedType(pt, typeVariableMap);
                    }
                    type = type.getEnclosingClass();
                }
            } catch (MalformedParameterizedTypeException ex) {
                // from getGenericSuperclass() - ignore and preserve previously accumulated type variables
            }

            typeVariableCache.put(clazz, typeVariableMap);
        }

        return typeVariableMap;
    }

    static Type extractBoundForTypeVariable(TypeVariable typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Object.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = extractBoundForTypeVariable((TypeVariable) bound);
        }
        return bound;
    }

    private static void extractTypeVariablesFromGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable, Type> typeVariableMap) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                populateTypeMapFromParameterizedType(pt, typeVariableMap);
                if (pt.getRawType() instanceof Class) {
                    extractTypeVariablesFromGenericInterfaces(
                            ((Class) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
                }
            } else if (genericInterface instanceof Class) {
                extractTypeVariablesFromGenericInterfaces(
                        ((Class) genericInterface).getGenericInterfaces(), typeVariableMap);
            }
        }
    }


    private static void populateTypeMapFromParameterizedType(ParameterizedType type, Map<TypeVariable, Type> typeVariableMap) {
        if (type.getRawType() instanceof Class) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                TypeVariable variable = typeVariables[i];
                if (actualTypeArgument instanceof Class) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof GenericArrayType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof ParameterizedType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof TypeVariable) {
                    // We have a type that is parameterized at instantiation time
                    // the nearest match on the bridge method will be the bounded type.
                    TypeVariable typeVariableArgument = (TypeVariable) actualTypeArgument;
                    Type resolvedType = typeVariableMap.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = extractBoundForTypeVariable(typeVariableArgument);
                    }
                    typeVariableMap.put(variable, resolvedType);
                }
            }
        }
    }

}
