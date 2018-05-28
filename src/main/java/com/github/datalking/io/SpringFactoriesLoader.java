package com.github.datalking.io;

import com.github.datalking.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yaoo on 5/28/18
 */
public abstract class SpringFactoriesLoader {

    /**
     * The location to look for the factories. Can be present in multiple JAR files.
     */
    private static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

    private static final Logger logger = LoggerFactory.getLogger(SpringFactoriesLoader.class);


    /**
     * Load the factory implementations of the given type from the default location,
     * using the given class loader.
     * <p>The returned factories are ordered in accordance with the OrderComparator.
     *
     * @param factoryClass the interface or abstract class representing the factory
     * @param classLoader  the ClassLoader to use for loading (can be {@code null} to use the default)
     */
//    public static <T> List<T> loadFactories(Class<T> factoryClass, ClassLoader classLoader) {
//        Assert.notNull(factoryClass, "'factoryClass' must not be null");
//        ClassLoader classLoaderToUse = classLoader;
//        if (classLoaderToUse == null) {
//            classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
//        }
//        List<String> factoryNames = loadFactoryNames(factoryClass, classLoaderToUse);
//        if (logger.isTraceEnabled()) {
//            logger.trace("Loaded [" + factoryClass.getName() + "] names: " + factoryNames);
//        }
//        List<T> result = new ArrayList<T>(factoryNames.size());
//        for (String factoryName : factoryNames) {
//            result.add(instantiateFactory(factoryName, factoryClass, classLoaderToUse));
//        }
//        OrderComparator.sort(result);
//        return result;
//    }
//
//    public static List<String> loadFactoryNames(Class<?> factoryClass, ClassLoader classLoader) {
//        String factoryClassName = factoryClass.getName();
//        try {
//            Enumeration<URL> urls = (classLoader != null ? classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
//                    ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
//            List<String> result = new ArrayList<String>();
//            while (urls.hasMoreElements()) {
//                URL url = urls.nextElement();
//                Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
//                String factoryClassNames = properties.getProperty(factoryClassName);
//                result.addAll(Arrays.asList(StringUtils.commaDelimitedListToStringArray(factoryClassNames)));
//            }
//            return result;
//        }
//        catch (IOException ex) {
//            throw new IllegalArgumentException("Unable to load [" + factoryClass.getName() +
//                    "] factories from location [" + FACTORIES_RESOURCE_LOCATION + "]", ex);
//        }
//    }
    private static <T> T instantiateFactory(String instanceClassName, Class<T> factoryClass, ClassLoader classLoader) {
        try {
            Class<?> instanceClass = ClassUtils.forName(instanceClassName, classLoader);
            if (!factoryClass.isAssignableFrom(instanceClass)) {
                throw new IllegalArgumentException("Class [" + instanceClassName + "] is not assignable to [" + factoryClass.getName() + "]");
            }

            return (T) instanceClass.newInstance();
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Cannot instantiate factory class: " + factoryClass.getName(), ex);
        }
    }

}
