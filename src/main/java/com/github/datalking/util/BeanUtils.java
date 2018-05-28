package com.github.datalking.util;

import com.github.datalking.beans.CachedIntrospectionResults;
import com.github.datalking.beans.GenericTypeAwarePropertyDescriptor;
import com.github.datalking.common.MethodParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * copied from spring
 */
public abstract class BeanUtils {

    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    // Effectively using a WeakHashMap as a Set
    private static final Map<Class<?>, Boolean> unknownEditorTypes = Collections.synchronizedMap(new WeakHashMap<>());

    public static <T> T instantiate(Class<T> clazz) {

        Assert.notNull(clazz, "Class must not be null");

        if (clazz.isInterface()) {
            try {
                throw new Exception(clazz + "Specified class is an interface");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object obj = null;

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (obj == null) {
            return null;
        }

        return (T) obj;

    }

    public static <T> T instantiateClass(Class<T> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            try {
                throw new Exception(clazz + "Specified class is an interface");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {

            return instantiateClass(clazz.getDeclaredConstructor());

        } catch (NoSuchMethodException ex) {
//            throw new Exception(clazz+"No default constructor found", ex);
            ex.printStackTrace();
        }

        return null;
    }

    public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) {
        Assert.isAssignable(assignableTo, clazz);
        return (T) instantiateClass(clazz);
    }

    /**
     * 通过java反射调用 Constructor.newInstance() 创建新实例
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) {
        Assert.notNull(ctor, "Constructor must not be null");
        ReflectionUtils.makeAccessible(ctor);

        Object obj = null;

        try {
            obj = ctor.newInstance(args);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (obj == null) {
            return null;
        }

        return (T) obj;
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return findDeclaredMethod(clazz, methodName, paramTypes);
        }
    }

    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
            }
            return null;
        }
    }

    public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
        if (targetMethod == null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
        }
        return targetMethod;
    }

    public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
        if (targetMethod == null && clazz.getSuperclass() != null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
        }
        return targetMethod;
    }


    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterTypes().length;
                if (targetMethod == null || numParams < targetMethod.getParameterTypes().length) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else {
                    if (targetMethod.getParameterTypes().length == numParams) {
                        // Additional candidate with same length
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName +
                    "' to a unique method. Attempted to resolve to overloaded method with " +
                    "the least number of parameters, but there were " +
                    numMethodsFoundWithCurrentMinimumArgs + " candidates.");
        }
        return targetMethod;
    }

    public static Method resolveSignature(String signature, Class<?> clazz) {
        Assert.hasText(signature, "'signature' must not be empty");
        Assert.notNull(clazz, "Class must not be null");
        int firstParen = signature.indexOf("(");
        int lastParen = signature.indexOf(")");
        if (firstParen > -1 && lastParen == -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected closing ')' for args list");
        } else if (lastParen > -1 && firstParen == -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected opening '(' for args list");
        } else if (firstParen == -1 && lastParen == -1) {
            return findMethodWithMinimalParameters(clazz, signature);
        } else {
            String methodName = signature.substring(0, firstParen);
            String[] parameterTypeNames =
                    StringUtils.commaDelimitedListToStringArray(signature.substring(firstParen + 1, lastParen));
            Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                String parameterTypeName = parameterTypeNames[i].trim();
                try {
                    parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" +
                            parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
                }
            }
            return findMethod(clazz, methodName, parameterTypes);
        }
    }

    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptors();
    }


    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {

        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptor(propertyName);
    }


    public static PropertyDescriptor findPropertyForMethod(Method method) {
        return findPropertyForMethod(method, method.getDeclaringClass());
    }


    public static PropertyDescriptor findPropertyForMethod(Method method, Class<?> clazz) {
        Assert.notNull(method, "Method must not be null");
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        for (PropertyDescriptor pd : pds) {
            if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
                return pd;
            }
        }
        return null;
    }


    public static PropertyEditor findEditorByConvention(Class<?> targetType) {
        if (targetType == null || targetType.isArray() || unknownEditorTypes.containsKey(targetType)) {
            return null;
        }
        ClassLoader cl = targetType.getClassLoader();
        if (cl == null) {
            try {
                cl = ClassLoader.getSystemClassLoader();
                if (cl == null) {
                    return null;
                }
            } catch (Throwable ex) {
                // e.g. AccessControlException on Google App Engine
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not access system ClassLoader: " + ex);
                }
                return null;
            }
        }
        String editorName = targetType.getName() + "Editor";
        try {
            Class<?> editorClass = cl.loadClass(editorName);
            if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Editor class [" + editorName +
                            "] does not implement [java.beans.PropertyEditor] interface");
                }
                unknownEditorTypes.put(targetType, Boolean.TRUE);
                return null;
            }
            return (PropertyEditor) instantiateClass(editorClass);
        } catch (ClassNotFoundException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("No property editor [" + editorName + "] found for type " +
                        targetType.getName() + " according to 'Editor' suffix convention");
            }
            unknownEditorTypes.put(targetType, Boolean.TRUE);
            return null;
        }
    }

    public static Class<?> findPropertyType(String propertyName, Class<?>... beanClasses) {
        if (beanClasses != null) {
            for (Class<?> beanClass : beanClasses) {
                PropertyDescriptor pd = getPropertyDescriptor(beanClass, propertyName);
                if (pd != null) {
                    return pd.getPropertyType();
                }
            }
        }
        return Object.class;
    }

    public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd) {
        if (pd instanceof GenericTypeAwarePropertyDescriptor) {
            return new MethodParameter(((GenericTypeAwarePropertyDescriptor) pd).getWriteMethodParameter());
        } else {
            return new MethodParameter(pd.getWriteMethod(), 0);
        }
    }

    public static boolean isSimpleProperty(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }


    public static boolean isSimpleValueType(Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() ||
                CharSequence.class.isAssignableFrom(clazz) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                clazz.equals(URI.class) || clazz.equals(URL.class) ||
                clazz.equals(Locale.class) || clazz.equals(Class.class);
    }


//    public static void copyProperties(Object source, Object target) {
//        copyProperties(source, target, null, (String[]) null);
//    }
//
//    public static void copyProperties(Object source, Object target, Class<?> editable) {
//        copyProperties(source, target, editable, (String[]) null);
//    }
//
//    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
//        copyProperties(source, target, null, ignoreProperties);
//    }

//    private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties) {
//
//        Assert.notNull(source, "Source must not be null");
//        Assert.notNull(target, "Target must not be null");
//
//        Class<?> actualEditable = target.getClass();
//        if (editable != null) {
//            if (!editable.isInstance(target)) {
//                throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
//                        "] not assignable to Editable class [" + editable.getName() + "]");
//            }
//            actualEditable = editable;
//        }
//        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
//        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);
//
//        for (PropertyDescriptor targetPd : targetPds) {
//            Method writeMethod = targetPd.getWriteMethod();
//            if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
//                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
//                if (sourcePd != null) {
//                    Method readMethod = sourcePd.getReadMethod();
//                    if (readMethod != null &&
//                            ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
//                        try {
//                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
//                                readMethod.setAccessible(true);
//                            }
//                            Object value = readMethod.invoke(source);
//                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
//                                writeMethod.setAccessible(true);
//                            }
//                            writeMethod.invoke(target, value);
//                        } catch (Throwable ex) {
//                            throw new FatalBeanException(
//                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
//                        }
//                    }
//                }
//            }
//        }
//    }

}
