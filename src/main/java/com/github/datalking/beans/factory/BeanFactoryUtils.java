package com.github.datalking.beans.factory;

import com.github.datalking.exception.NoSuchBeanDefinitionException;
import com.github.datalking.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BeanFactory工具类
 */
public abstract class BeanFactoryUtils {

    public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";


    public static boolean isFactoryDereference(String name) {
        return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
    }

    public static String transformedBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
        String beanName = name;
        while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
        }
        return beanName;
    }


    public static boolean isGeneratedBeanName(String name) {
        return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
    }


    public static String originalBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
        int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
        return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
    }


    public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
        return beanNamesIncludingAncestors(lbf).length;
    }

    /**
     * Return all bean names in the factory, including ancestor factories.
     *
     * @param lbf the bean factory
     * @return the array of matching bean names, or an empty array if none
     * @see #beanNamesForTypeIncludingAncestors
     */
    public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
        return beanNamesForTypeIncludingAncestors(lbf, Object.class);
    }


    /**
     * Get all bean names for the given type, including those defined in ancestor
     * factories. Will return unique names in case of overridden bean definitions.
     * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
     * will get initialized. If the object created by the FactoryBean doesn't match,
     * the raw FactoryBean itself will be matched against the type.
     * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
     * includes prototypes and FactoryBeans.
     *
     * @param lbf  the bean factory
     * @param type the type that beans must match
     * @return the array of matching bean names, or an empty array if none
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type);

//        if (lbf instanceof HierarchicalBeanFactory) {
//            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//                String[] parentResult = beanNamesForTypeIncludingAncestors(
//                        (ListableBeanFactory) hbf.getParentBeanFactory(), type);
//                List<String> resultList = new ArrayList<String>();
//                resultList.addAll(Arrays.asList(result));
//                for (String beanName : parentResult) {
//                    if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
//                        resultList.add(beanName);
//                    }
//                }
//                result = StringUtils.toStringArray(resultList);
//            }
//        }

        return result;
    }


    public static String[] beanNamesForTypeIncludingAncestors(
            ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type);

//        if (lbf instanceof HierarchicalBeanFactory) {
//            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//                String[] parentResult = beanNamesForTypeIncludingAncestors(
//                        (ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
//                List<String> resultList = new ArrayList<String>();
//                resultList.addAll(Arrays.asList(result));
//                for (String beanName : parentResult) {
//                    if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
//                        resultList.add(beanName);
//                    }
//                }
//                result = StringUtils.toStringArray(resultList);
//            }
//        }

        return result;
    }


    public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type) {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> result = new LinkedHashMap<String, T>(4);
        result.putAll(lbf.getBeansOfType(type));
//        if (lbf instanceof HierarchicalBeanFactory) {
//            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//                Map<String, T> parentResult = beansOfTypeIncludingAncestors(
//                        (ListableBeanFactory) hbf.getParentBeanFactory(), type);
//                for (Map.Entry<String, T> entry : parentResult.entrySet()) {
//                    String beanName = entry.getKey();
//                    if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
//                        result.put(beanName, entry.getValue());
//                    }
//                }
//            }
//        }
        return result;
    }


    public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");

        Map<String, T> result = new LinkedHashMap<String, T>(4);
        result.putAll(lbf.getBeansOfType(type));

//        if (lbf instanceof HierarchicalBeanFactory) {
//            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//                Map<String, T> parentResult = beansOfTypeIncludingAncestors(
//                        (ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
//                for (Map.Entry<String, T> entry : parentResult.entrySet()) {
//                    String beanName = entry.getKey();
//                    if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
//                        result.put(beanName, entry.getValue());
//                    }
//                }
//            }
//        }
        return result;
    }


    public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type) {

        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
        return uniqueBean(type, beansOfType);
    }


    public static <T> T beanOfTypeIncludingAncestors(
            ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) {

        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
        return uniqueBean(type, beansOfType);
    }


    public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> beansOfType = lbf.getBeansOfType(type);
        return uniqueBean(type, beansOfType);
    }


    public static <T> T beanOfType(
            ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> beansOfType = lbf.getBeansOfType(type);
        return uniqueBean(type, beansOfType);
    }


    private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
        int nrFound = matchingBeans.size();
        if (nrFound == 1) {
            return matchingBeans.values().iterator().next();
        } else if (nrFound > 1) {

            try {
                throw new Exception(type + "," + matchingBeans.keySet());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new NoSuchBeanDefinitionException(type.getName());
        }

        return null;
    }

}
