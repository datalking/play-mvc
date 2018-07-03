
package com.github.datalking.util;

import com.github.datalking.common.meta.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnnotationUtils {

    public static final String VALUE = "value";

    private static final String REPEATABLE_CLASS_NAME = "java.lang.annotation.Repeatable";

    private static final Map<AnnotationCacheKey, Annotation> findAnnotationCache = new ConcurrentHashMap<>(256);

    private static final Map<AnnotationCacheKey, Boolean> metaPresentCache = new ConcurrentHashMap<>(256);

    private static final Map<Class<?>, Boolean> annotatedInterfaceCache = new ConcurrentHashMap<>(256);

    private static final Map<Class<? extends Annotation>, Boolean> synthesizableCache = new ConcurrentHashMap<>(256);

    private static final Map<Class<? extends Annotation>, Map<String, List<String>>> attributeAliasesCache = new ConcurrentHashMap<>(256);

    private static final Map<Class<? extends Annotation>, List<Method>> attributeMethodsCache = new ConcurrentHashMap<>(256);

    /**
     * 获取注解的所有属性键值对
     */
    public static Map<String, Object> getAnnotationAttributes(Class clazz, Class annotationClass) {
        Assert.notNull(annotationClass, "输入的注解类不能为空");

        if (!clazz.isAnnotationPresent(annotationClass)) {
            return null;
        }

        // 保存注解的所有属性键值对，属性名 -> 属性值
        Map<String, Object> annoMap = new LinkedHashMap<>();

        Annotation a = clazz.getAnnotation(annotationClass);
        if (a != null) {
            Class<? extends Annotation> type = a.annotationType();
            for (Method method : type.getDeclaredMethods()) {
                Object value = null;
                try {
                    value = method.invoke(a);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                annoMap.put(method.getName(), value);
            }
        }

        return annoMap;
    }

    /**
     * 获取注解的某个属性值
     */
    public static Object getAnnotationValue(Class clazz, Class annotationClass, String name) {
        Map<String, Object> annoMap = getAnnotationAttributes(clazz, annotationClass);
        return annoMap.get(name);
    }

    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
        Assert.notNull(method, "Method must not be null");
        if (annotationType == null) {
            return null;
        }

        AnnotationCacheKey cacheKey = new AnnotationCacheKey(method, annotationType);
        A result = (A) findAnnotationCache.get(cacheKey);

        if (result == null) {
//            Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
            result = findAnnotation((AnnotatedElement) method, annotationType);

            if (result == null) {
                result = searchOnInterfaces(method, annotationType, method.getDeclaringClass().getInterfaces());
            }

            Class<?> clazz = method.getDeclaringClass();
            while (result == null) {
                clazz = clazz.getSuperclass();
                if (clazz == null || Object.class == clazz) {
                    break;
                }
                try {
                    Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
//                    Method resolvedEquivalentMethod = BridgeMethodResolver.findBridgedMethod(equivalentMethod);
                    result = findAnnotation((AnnotatedElement) equivalentMethod, annotationType);
                } catch (NoSuchMethodException ex) {
                    // No equivalent method found
                }
                if (result == null) {
                    result = searchOnInterfaces(method, annotationType, clazz.getInterfaces());
                }
            }

            if (result != null) {
                findAnnotationCache.put(cacheKey, result);
            }
        }

        return result;
    }

    public static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        Assert.notNull(annotatedElement, "AnnotatedElement must not be null");
        if (annotationType == null) {
            return null;
        }

        A ann = findAnnotation(annotatedElement, annotationType, new HashSet<>());
        return ann;
    }

    private static <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited) {
        try {
            Annotation[] anns = annotatedElement.getDeclaredAnnotations();
            for (Annotation ann : anns) {
                if (ann.annotationType() == annotationType) {
                    return (A) ann;
                }
            }
            for (Annotation ann : anns) {
                if (!isInJavaLangAnnotationPackage(ann) && visited.add(ann)) {
                    A annotation = findAnnotation(ann.annotationType(), annotationType, visited);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean isInJavaLangAnnotationPackage(Annotation annotation) {
        return (annotation != null && isInJavaLangAnnotationPackage(annotation.annotationType()));
    }

    static boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
        return (annotationType != null && isInJavaLangAnnotationPackage(annotationType.getName()));
    }

    public static boolean isInJavaLangAnnotationPackage(String annotationType) {
        return (annotationType != null && annotationType.startsWith("java.lang.annotation"));
    }


    private static <A extends Annotation> A searchOnInterfaces(Method method, Class<A> annotationType, Class<?>... ifcs) {
        A annotation = null;
        for (Class<?> iface : ifcs) {
            if (isInterfaceWithAnnotatedMethods(iface)) {
                try {
                    Method equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                    annotation = getAnnotation(equivalentMethod, annotationType);
                } catch (NoSuchMethodException ex) {
                    // Skip this interface - it doesn't have the method...
                }
                if (annotation != null) {
                    break;
                }
            }
        }
        return annotation;
    }

    static boolean isInterfaceWithAnnotatedMethods(Class<?> iface) {
        Boolean found = annotatedInterfaceCache.get(iface);
        if (found != null) {
            return found;
        }
        found = Boolean.FALSE;
        for (Method ifcMethod : iface.getMethods()) {
            try {
                if (ifcMethod.getAnnotations().length > 0) {
                    found = Boolean.TRUE;
                    break;
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        annotatedInterfaceCache.put(iface, found);
        return found;
    }

    public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        return getAnnotationAttributes(annotation, false, false);
    }

    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation,
                                                               boolean classValuesAsString,
                                                               boolean nestedAnnotationsAsMap) {

        AnnotationAttributes attrs = new AnnotationAttributes();
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                try {
                    Object value = method.invoke(annotation);
                    if (classValuesAsString) {
                        if (value instanceof Class) {
                            value = ((Class<?>) value).getName();
                        } else if (value instanceof Class[]) {
                            Class<?>[] clazzArray = (Class[]) value;
                            String[] newValue = new String[clazzArray.length];
                            for (int i = 0; i < clazzArray.length; i++) {
                                newValue[i] = clazzArray[i].getName();
                            }
                            value = newValue;
                        }
                    }
                    if (nestedAnnotationsAsMap && value instanceof Annotation) {
                        attrs.put(method.getName(),
                                getAnnotationAttributes((Annotation) value, classValuesAsString, true));
                    } else if (nestedAnnotationsAsMap && value instanceof Annotation[]) {
                        Annotation[] realAnnotations = (Annotation[]) value;
                        AnnotationAttributes[] mappedAnnotations = new AnnotationAttributes[realAnnotations.length];
                        for (int i = 0; i < realAnnotations.length; i++) {
                            mappedAnnotations[i] = getAnnotationAttributes(realAnnotations[i], classValuesAsString, true);
                        }
                        attrs.put(method.getName(), mappedAnnotations);
                    } else {
                        attrs.put(method.getName(), value);
                    }
                } catch (Exception ex) {
                    throw new IllegalStateException("Could not obtain annotation attribute values", ex);
                }
            }
        }
        return attrs;
    }


    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
//        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
        return getAnnotation((AnnotatedElement) method, annotationType);
    }

    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        try {
            A annotation = annotatedElement.getAnnotation(annotationType);
            if (annotation == null) {
                for (Annotation metaAnn : annotatedElement.getAnnotations()) {
                    annotation = metaAnn.annotationType().getAnnotation(annotationType);
                    if (annotation != null) {
                        break;
                    }
                }
            }
            return annotation;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static final class AnnotationCacheKey implements Comparable<AnnotationCacheKey> {

        private final AnnotatedElement element;

        private final Class<? extends Annotation> annotationType;

        public AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
            this.element = element;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AnnotationCacheKey)) {
                return false;
            }
            AnnotationCacheKey otherKey = (AnnotationCacheKey) other;
            return (this.element.equals(otherKey.element) && this.annotationType.equals(otherKey.annotationType));
        }

        @Override
        public int hashCode() {
            return (this.element.hashCode() * 29 + this.annotationType.hashCode());
        }

        @Override
        public String toString() {
            return "@" + this.annotationType + " on " + this.element;
        }

        @Override
        public int compareTo(AnnotationCacheKey other) {
            int result = this.element.toString().compareTo(other.element.toString());
            if (result == 0) {
                result = this.annotationType.getName().compareTo(other.annotationType.getName());
            }
            return result;
        }
    }


    public static Object getValue(Annotation annotation) {
        return getValue(annotation, VALUE);
    }

    public static Object getValue(Annotation annotation, String attributeName) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName);
            ReflectionUtils.makeAccessible(method);
            return method.invoke(annotation);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Object getDefaultValue(Annotation annotation) {
        return getDefaultValue(annotation, VALUE);
    }

    public static Object getDefaultValue(Annotation annotation, String attributeName) {
        return getDefaultValue(annotation.annotationType(), attributeName);
    }

    public static Object getDefaultValue(Class<? extends Annotation> annotationType, String attributeName) {
        try {
            return annotationType.getDeclaredMethod(attributeName).getDefaultValue();
        } catch (Exception ex) {
            return null;
        }
    }

}
