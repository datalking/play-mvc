package com.github.datalking.beans;

import com.github.datalking.exception.BeansException;
import com.github.datalking.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 保存Introspector分析的类的元数据 单例
 *
 * @author yaoo on 5/28/18
 */
public class CachedIntrospectionResults {

    private static final Logger logger = LoggerFactory.getLogger(CachedIntrospectionResults.class);

//    public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";

    //    private static final boolean shouldIntrospectorIgnoreBeaninfoClasses = SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);
    // 默认获取所有bean的对应类的元信息
    private static final boolean shouldIntrospectorIgnoreBeaninfoClasses = false;

    //    private static List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());
    private static List<BeanInfoFactory> beanInfoFactories = new ArrayList<>();

    static {
        beanInfoFactories.add(new ExtendedBeanInfoFactory());
    }

    static final Set<ClassLoader> acceptedClassLoaders = new HashSet<>();

    // 缓存创建class元信息的CachedIntrospectionResults，class -> 对应的CachedIntrospectionResults
    static final Map<Class<?>, Object> classCache = new WeakHashMap<>();

    /**
     * 一般代替构造方法
     */
    public static CachedIntrospectionResults forClass(Class<?> beanClass) {
        CachedIntrospectionResults results;
        Object value;
        synchronized (classCache) {
            value = classCache.get(beanClass);
        }

        /// 获取vlaue对应的CachedIntrospectionResults
        if (value instanceof Reference) {
            Reference<CachedIntrospectionResults> ref = (Reference<CachedIntrospectionResults>) value;
            results = ref.get();
        } else {
            results = (CachedIntrospectionResults) value;
        }

        if (results == null) {
//            if (ClassUtils.isCacheSafe(beanClass, CachedIntrospectionResults.class.getClassLoader()) ||
//                    isClassLoaderAccepted(beanClass.getClassLoader())) {
//                results = new CachedIntrospectionResults(beanClass);
//                synchronized (classCache) {
//                    classCache.put(beanClass, results);
//                }
//            } else {

            // 创建class对应的CachedIntrospectionResults对象
            results = new CachedIntrospectionResults(beanClass);

            synchronized (classCache) {
                // 加入缓存
                classCache.put(beanClass, new SoftReference<>(results));
            }
//            }
        }

        return results;
    }

    // ======= #region - classloader method ========

    public static void acceptClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            synchronized (acceptedClassLoaders) {
                acceptedClassLoaders.add(classLoader);
            }
        }
    }

    public static void clearClassLoader(ClassLoader classLoader) {
        synchronized (classCache) {
            for (Iterator<Class<?>> it = classCache.keySet().iterator(); it.hasNext(); ) {
                Class<?> beanClass = it.next();
                if (isUnderneathClassLoader(beanClass.getClassLoader(), classLoader)) {
                    it.remove();
                }
            }
        }
        synchronized (acceptedClassLoaders) {
            for (Iterator<ClassLoader> it = acceptedClassLoaders.iterator(); it.hasNext(); ) {
                ClassLoader registeredLoader = it.next();
                if (isUnderneathClassLoader(registeredLoader, classLoader)) {
                    it.remove();
                }
            }
        }
    }

    private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
        // Iterate over array copy in order to avoid synchronization for the entire
        // ClassLoader check (avoiding a synchronized acceptedClassLoaders Iterator).
        ClassLoader[] acceptedLoaderArray;
        synchronized (acceptedClassLoaders) {
            acceptedLoaderArray = acceptedClassLoaders.toArray(new ClassLoader[acceptedClassLoaders.size()]);
        }
        for (ClassLoader acceptedLoader : acceptedLoaderArray) {
            if (isUnderneathClassLoader(classLoader, acceptedLoader)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUnderneathClassLoader(ClassLoader candidate, ClassLoader parent) {
        if (candidate == parent) {
            return true;
        }
        if (candidate == null) {
            return false;
        }
        ClassLoader classLoaderToCheck = candidate;
        while (classLoaderToCheck != null) {
            classLoaderToCheck = classLoaderToCheck.getParent();
            if (classLoaderToCheck == parent) {
                return true;
            }
        }
        return false;
    }

    // ======= #endregion - classloader method ========

    /**
     * bean内省信息操作对象
     */
    private final BeanInfo beanInfo;

    /**
     * 缓存bean属性的PropertyDescriptor
     */
    private final Map<String, PropertyDescriptor> propertyDescriptorCache;

    private CachedIntrospectionResults(Class<?> beanClass) {
        try {

            BeanInfo beanInfo = null;

            /// 如果工厂中已经有bean信息，则停止循环
            for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
                beanInfo = beanInfoFactory.getBeanInfo(beanClass);
                if (beanInfo != null) {
                    break;
                }
            }

            if (beanInfo == null) {
                // If none of the factories supported the class, fall back to the default
//                beanInfo = (shouldIntrospectorIgnoreBeaninfoClasses ?
//                        Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) :
//                        Introspector.getBeanInfo(beanClass));

                // 通过java内省获取bean信息
                beanInfo = Introspector.getBeanInfo(beanClass);
            }
            this.beanInfo = beanInfo;

//            if (!shouldIntrospectorIgnoreBeaninfoClasses) {

            Class<?> classToFlush = beanClass;
            do {
                // Introspector缓存了bean属性和方法元信息，可能导致内存泄露，多是热部署时的问题，这里清除该类及其父类的缓存
                Introspector.flushFromCaches(classToFlush);
                classToFlush = classToFlush.getSuperclass();
            }
            while (classToFlush != null && classToFlush != Object.class);
//            }

            this.propertyDescriptorCache = new LinkedHashMap<>();

            // This call is slow so we do it once.
            PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (Class.class.equals(beanClass) &&
                        ("classLoader".equals(pd.getName()) || "protectionDomain".equals(pd.getName()))) {
                    // Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
                    continue;
                }

                // 创建PropertyDescriptor子类的对象
                pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
                // 添加到缓存
                this.propertyDescriptorCache.put(pd.getName(), pd);
            }
        } catch (IntrospectionException ex) {
            throw new BeansException("Failed to obtain BeanInfo for class [" + beanClass.getName() + "]", ex);
        }
    }

    BeanInfo getBeanInfo() {
        return this.beanInfo;
    }

    Class<?> getBeanClass() {
        return this.beanInfo.getBeanDescriptor().getBeanClass();
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        PropertyDescriptor pd = this.propertyDescriptorCache.get(name);
        if (pd == null && StringUtils.hasLength(name)) {
            // Same lenient fallback checking as in PropertyTypeDescriptor...
            pd = this.propertyDescriptorCache.get(name.substring(0, 1).toLowerCase() + name.substring(1));
            if (pd == null) {
                pd = this.propertyDescriptorCache.get(name.substring(0, 1).toUpperCase() + name.substring(1));
            }
        }
        return (pd == null || pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
                buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] pds = new PropertyDescriptor[this.propertyDescriptorCache.size()];
        int i = 0;
        for (PropertyDescriptor pd : this.propertyDescriptorCache.values()) {
            pds[i] = (pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
                    buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
            i++;
        }
        return pds;
    }

    private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
        try {
            return new GenericTypeAwarePropertyDescriptor(
                    beanClass,
                    pd.getName(),
                    pd.getReadMethod(),
                    pd.getWriteMethod(),
                    pd.getPropertyEditorClass()
            );

        } catch (IntrospectionException ex) {
            throw new BeansException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
        }
    }

}
