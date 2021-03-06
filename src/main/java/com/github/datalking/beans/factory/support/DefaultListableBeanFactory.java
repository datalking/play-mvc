package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryUtils;
import com.github.datalking.beans.factory.FactoryBean;
import com.github.datalking.beans.factory.ObjectFactory;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.config.ConstructorArgumentValues;
import com.github.datalking.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import com.github.datalking.beans.factory.config.DependencyDescriptor;
import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.exception.BeansException;
import com.github.datalking.exception.NoSuchBeanDefinitionException;
import com.github.datalking.exception.NoUniqueBeanDefinitionException;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;

import javax.inject.Provider;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanFactory默认实现类
 *
 * @author yaoo on 4/3/18
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

    private static Class<?> javaxInjectProviderClass = null;

    static {
        try {
            javaxInjectProviderClass =
                    ClassUtils.forName("javax.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // JSR-330 API not available - Provider interface simply not supported then.
            ex.printStackTrace();
        }
    }

    //    private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();
    private AutowireCandidateResolver autowireCandidateResolver = new QualifierAnnotationAutowireCandidateResolver();

    // 所有BeanDefinition
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    // 所有bean名称
    private volatile List<String> beanDefinitionNames = new ArrayList<>(256);
    // 手动注册的bean实例名称
    private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

    private Map<Class<?>, Object> resolvableDependencies = new HashMap<>(16);

    //所有单例和非单例的bean
//    private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);
//    //是否允许同名bean注册
//    private boolean allowBeanDefinitionOverriding = true;
//    private volatile boolean configurationFrozen = false;

    public DefaultListableBeanFactory() {
        super();
    }

    // ======== BeanDefinitionRegistry interface ========

    /**
     * 注册beanDefinition到beanDefinitionMap
     *
     * @param beanName       bean名称
     * @param beanDefinition bean属性元信息对象
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {

        synchronized (this.beanDefinitionMap) {
            //存储beanDefinition到map
            this.beanDefinitionMap.put(beanName, beanDefinition);

            Set<String> updatedDefinitions = new HashSet<>(this.beanDefinitionNames.size() + 1);
            updatedDefinitions.addAll(this.beanDefinitionNames);
            updatedDefinitions.add(beanName);
            this.beanDefinitionNames = new ArrayList<>(updatedDefinitions);
        }

    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
        super.registerSingleton(beanName, singletonObject);

        if (hasBeanCreationStarted()) {
            synchronized (this.beanDefinitionMap) {
                if (!this.beanDefinitionMap.containsKey(beanName)) {

                    Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames.size() + 1);
                    updatedSingletons.addAll(this.manualSingletonNames);
                    updatedSingletons.add(beanName);
                    this.manualSingletonNames = updatedSingletons;
                }
            }
        } else {
            if (!this.beanDefinitionMap.containsKey(beanName)) {

                this.manualSingletonNames.add(beanName);
            }
        }
    }

//    @Override
//    public void removeBeanDefinition(String beanName) {
//
//        BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
//        if (bd == null) throw new NoSuchBeanDefinitionException(beanName);
//        synchronized (this.beanDefinitionMap) {
//            List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
//            updatedDefinitions.remove(beanName);
//            this.beanDefinitionNames = updatedDefinitions;
//        }
//
//    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return bd;
    }

    /**
     * 通过调用getBean()来实例化bean
     */
    @Override
    public void preInstantiateSingletons() {

        // 遍历的是副本，遍历过程中仍可以添加BeanDefinition
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

        for (String beanName : beanNames) {

            if (beanName.equals("dataSource")) {
                System.out.println("====preInstantiateSingletons: " + beanName);
            }

            /// 如果是FactoryBean，则计算bean
            if (isFactoryBean(beanName)) {

                // FactoryBean实例化时特殊处理
                final FactoryBean<?> factory = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);

            }
            /// 如果不是FactoryBean，则直接实例化
            else {

                getBean(beanName);
            }

        }

        /// 触发afterSingletonsInstantiated()后处理器
        for (String beanName : beanNames) {

        }
    }

    // ======== ConfiguraleListableBeanFactory ========

    public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
        Assert.notNull(dependencyType, "Dependency type must not be null");
        if (autowiredValue != null) {
            if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
                throw new IllegalArgumentException("Value [" + autowiredValue +
                        "] does not implement specified dependency type [" + dependencyType.getName() + "]");
            }
            this.resolvableDependencies.put(dependencyType, autowiredValue);
        }
    }

    public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) {

        // Consider FactoryBeans as autowiring candidates.
        boolean isFactoryBean = (descriptor != null
                && descriptor.getDependencyType() != null
                && FactoryBean.class.isAssignableFrom(descriptor.getDependencyType()));

        if (isFactoryBean) {
            beanName = BeanFactoryUtils.transformedBeanName(beanName);
        }

        if (containsBeanDefinition(beanName)) {
            return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanName), descriptor);
        } else if (containsSingleton(beanName)) {
            return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor);
        } else if (getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
            // No bean definition found in this factory -> delegate to parent.
            return ((ConfigurableListableBeanFactory) getParentBeanFactory()).isAutowireCandidate(beanName, descriptor);
        } else {
            return true;
        }
    }

    protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor) {

        resolveBeanClass(mbd, beanName);

        if (mbd.isFactoryMethodUnique) {
            boolean resolve;
//            synchronized (mbd.constructorArgumentLock) {
            resolve = (mbd.resolvedConstructorOrFactoryMethod == null);
//            }
            if (resolve) {
                new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
            }
        }
        BeanDefinitionHolder bdHolder = new BeanDefinitionHolder(mbd, beanName, getAliases(beanName));
        return this.autowireCandidateResolver.isAutowireCandidate(bdHolder, descriptor);
    }

    public String[] getAliases(String name) {

        return new String[0];
    }

    // ======== ListableBeanFactory interface ========
    @Override
    public boolean containsBeanDefinition(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionNames.toArray(new String[this.beanDefinitionNames.size()]);
    }

    /**
     * 获取指定class类型对应的bean名称
     * 包括扫描的bean
     */
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();

        for (String beanName : this.beanDefinitionNames) {

            // 各种BeanDefinition添加进 mergedBeanDefinitions
            RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

            // 当前迭代的bean的class
            Class clazz = null;
            // 为true时针对泛型处理
            boolean clazzIsFactory = false;

            if (mbd.hasBeanClass()) {
                clazz = mbd.getBeanClass();
            }

            /// 若beanClass存在
            if (clazz != null) {

                /// 若是普通bean，且是type的子类，则满足条件
                if (type.isAssignableFrom(clazz)) {
                    result.add(beanName);
                    continue;
                }

                /// 若是普通bean，且不是FactoryBean的直接子类，则跳过
                if (!FactoryBean.class.isAssignableFrom(clazz)) {
                    continue;
                }

                /// 若是FactoryBean的子类，则需要特殊处理
                if (FactoryBean.class.isAssignableFrom(clazz)) {
                    // 为true时针对泛型处理
                    clazzIsFactory = true;
                }
            }

            /// 若是Class是FactoryBean或本身是FactoryBean
            if (clazzIsFactory || isFactoryBean(beanName) || mbd.getFactoryMethodName() != null) {

                ConstructorArgumentValues args = mbd.getConstructorArgumentValues();

                /// 将@Bean方法的返回值类型作为bean类型
                if (mbd.getFactoryMethodName() != null) {

                    if (mbd instanceof ConfigurationClassBeanDefinition) {
                        String returnTypeName = ((ConfigurationClassBeanDefinition) mbd).getFactoryMethodMetadata().getReturnTypeName();
                        Class returnTypeClass = null;
                        try {
                            returnTypeClass = Class.forName(returnTypeName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        /// 若返回值类型直接是匹配的类型的子类，则符合
                        if (type.isAssignableFrom(returnTypeClass)) {
                            result.add(beanName);
                            continue;
                        }

                        /// 若返回值类型不是匹配类型的直接子类，而是FactoryBean，且FactoryBean的泛型恰好是匹配类型的子类，则也符合
                        // todo 更通用的方法
                        if (FactoryBean.class.isAssignableFrom(returnTypeClass)) {

                            /// 根据FactoryBean接口中定义的方法来确定可创建的bean类型
                            Method m = null;
                            try {
                                m = returnTypeClass.getMethod("getObject");
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            if (m != null) {
                                Class<?> factoryObjType = m.getReturnType();
                                if (type.isAssignableFrom(factoryObjType)) {
                                    result.add(beanName);
                                    continue;
                                }
                            }
                        }
                    }
                }
                /// 从泛型参数中获取bean类型 todo 说明场景
                else if (!args.isEmpty()) {
                    Map<Integer, ValueHolder> indexedArgumentValues = args.getIndexedArgumentValues();
                    List<ValueHolder> genericArgumentValues = args.getGenericArgumentValues();
//                    for (Map.Entry<Integer, ValueHolder> entry : indexedArgumentValues.entrySet()) {
//                        {
//                            String typeName = entry.getValue().getType();
//                            try {
//                                clazz = Class.forName(typeName);
//                            } catch (ClassNotFoundException e) {
//                                e.printStackTrace();
//                            }
//                            if (type.isAssignableFrom(clazz)) {
//                                result.add(beanName);
//                            }
//                        }
//                    }
                    if (genericArgumentValues != null && !genericArgumentValues.isEmpty()) {
                        for (ValueHolder v : genericArgumentValues) {
                            String typeName = (String) v.getValue();
                            try {
                                clazz = Class.forName(typeName);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (type.isAssignableFrom(clazz)) {
                                result.add(beanName);
                                continue;
                            }
                        }
                    }

                } else {

                }

            }
        }

        /// 再在手动注册的bean里面找
        for (String beanName : this.manualSingletonNames) {
            if (isFactoryBean(beanName)) {
                if (isTypeMatch(beanName, type)) {
                    result.add(beanName);
                    continue;
                }
                // In case of FactoryBean, try to match FactoryBean itself next.
                beanName = FACTORY_BEAN_PREFIX + beanName;
            }
            // Match raw bean instance (might be raw FactoryBean).
            if (isTypeMatch(beanName, type)) {
                result.add(beanName);
            }
        }

        if (result.size() == 0) {
            return new String[0];
        }

        return result.toArray(new String[result.size()]);
    }


    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {

        String[] beanNames = getBeanNamesForType(type);

        Map<String, T> result = new LinkedHashMap<>(beanNames.length);
        for (String name : beanNames) {
            result.put(name, (T) getBean(name));
        }

        return result;
    }


    // ======== AutowireCapableBeanFactory interface ========

    public Object resolveDependency(DependencyDescriptor descriptor, String beanName) {

        return resolveDependency(descriptor, beanName, null, null);
    }

    public Object resolveDependency(DependencyDescriptor descriptor,
                                    String beanName,
                                    Set<String> autowiredBeanNames,
                                    TypeConverter typeConverter) {

        descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());

        if (descriptor.getDependencyType().equals(ObjectFactory.class)) {

            return new DependencyObjectFactory(descriptor, beanName);
        } else if (descriptor.getDependencyType().equals(javaxInjectProviderClass)) {

            return new DependencyProviderFactory().createDependencyProvider(descriptor, beanName);
        } else {
            Class<?> clazz = descriptor.getDependencyType();
            /// 可能返回的是代理对象，如mybatis的mapper
            return doResolveDependency(descriptor, clazz, beanName, autowiredBeanNames, typeConverter);
        }
    }

    protected Object doResolveDependency(DependencyDescriptor descriptor,
                                         Class<?> type,
                                         String beanName,
                                         Set<String> autowiredBeanNames,
                                         TypeConverter typeConverter) {

        // 获取@Value注解的值用来注入，可能含有占位符
        Object value = this.autowireCandidateResolver.getSuggestedValue(descriptor);

        /// 若已获取到注解中值，则解析占位符后直接注入字段
        if (value != null) {
            if (value instanceof String) {

                //==== 使用 PropertySourcesPlaceholderConfigurer 解析占位符
                String strVal = resolveEmbeddedValue((String) value);
                BeanDefinition bd = (beanName != null && containsBean(beanName) ? getMergedBeanDefinition(beanName) : null);

                // 这里可以更改值，默认不更改
                value = evaluateBeanDefinitionString(strVal, bd);
            }

            TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            return (descriptor.getField() != null ?
                    converter.convertIfNecessary(value, type, descriptor.getField()) :
                    converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
        }

        /// 若type是数组
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType, descriptor);
            if (matchingBeans.isEmpty()) {
                if (descriptor.isRequired()) {
                    raiseNoSuchBeanDefinitionException(componentType, "array of " + componentType.getName(), descriptor);
                }
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            return converter.convertIfNecessary(matchingBeans.values(), type);
        }
        /// 若type是Collection的子类
        else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
            Class<?> elementType = descriptor.getCollectionType();
            if (elementType == null) {
                if (descriptor.isRequired()) {
                    throw new BeansException("No element type declared for collection [" + type.getName() + "]");
                }
                return null;
            }
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType, descriptor);
            if (matchingBeans.isEmpty()) {
                if (descriptor.isRequired()) {
                    raiseNoSuchBeanDefinitionException(elementType, "collection of " + elementType.getName(), descriptor);
                }
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            return converter.convertIfNecessary(matchingBeans.values(), type);
        }
        /// 若type是Map的子类
        else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
            Class<?> keyType = descriptor.getMapKeyType();
            if (keyType == null || !String.class.isAssignableFrom(keyType)) {
                if (descriptor.isRequired()) {
                    throw new BeansException("Key type [" + keyType + "] of map [" + type.getName() +
                            "] must be assignable to [java.lang.String]");
                }
                return null;
            }
            Class<?> valueType = descriptor.getMapValueType();
            if (valueType == null) {
                if (descriptor.isRequired()) {
                    throw new BeansException("No value type declared for map [" + type.getName() + "]");
                }
                return null;
            }
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType, descriptor);
            if (matchingBeans.isEmpty()) {
                if (descriptor.isRequired()) {
                    raiseNoSuchBeanDefinitionException(valueType, "map with value type " + valueType.getName(), descriptor);
                }
                return null;
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.addAll(matchingBeans.keySet());
            }
            return matchingBeans;
        }
        /// 若type是普通Class
        else {

            //==== 查找候选Bean并通过getBean()实例化，保存到matchingBeans
            Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);

            if (matchingBeans.isEmpty()) {
                if (descriptor.isRequired()) {
                    raiseNoSuchBeanDefinitionException(type, "", descriptor);
                }
                return null;
            }

            // 保存要返回的对象bean名
            String autowiredBeanName;
            // 保存要返回的对象
            Object instanceCandidate;

            /// 若存在多个候选bean，则挑选最主要的bean
            if (matchingBeans.size() > 1) {

                // 挑选最主要的bean
                autowiredBeanName = determinePrimaryCandidate(matchingBeans, descriptor);

                if (autowiredBeanName == null) {
                    throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
                }

                instanceCandidate = matchingBeans.get(autowiredBeanName);
            } else {
                /// 若只存在1个候选bean，则直接作为主要bean
                Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
                autowiredBeanName = entry.getKey();
                instanceCandidate = entry.getValue();
            }

            if (autowiredBeanNames != null) {
                autowiredBeanNames.add(autowiredBeanName);
            }

            // ==== 若是class，则直接调用getBean()返回实例化的对象
            Object obj = (instanceCandidate instanceof Class
                    ? descriptor.resolveCandidate(autowiredBeanName, type, this)
                    : instanceCandidate);

            return obj;
        }
    }

    /**
     * 从多个候选bean中挑选主要bean
     */
    protected String determinePrimaryCandidate(Map<String, Object> candidateBeans, DependencyDescriptor descriptor) {

        String primaryBeanName = null;
        String fallbackBeanName = null;

        for (Map.Entry<String, Object> entry : candidateBeans.entrySet()) {
            String candidateBeanName = entry.getKey();
            Object beanInstance = entry.getValue();

            // BeanDefinition的primary默认false
            if (isPrimary(candidateBeanName, beanInstance)) {
                if (primaryBeanName != null) {
                    boolean candidateLocal = containsBeanDefinition(candidateBeanName);
                    boolean primaryLocal = containsBeanDefinition(primaryBeanName);

                    if (candidateLocal == primaryLocal) {
                        throw new NoUniqueBeanDefinitionException(descriptor.getDependencyType(), candidateBeans.size(), "more than one 'primary' bean found among candidates: " + candidateBeans.keySet());
                    } else if (candidateLocal && !primaryLocal) {
                        primaryBeanName = candidateBeanName;
                    }

                } else {
                    primaryBeanName = candidateBeanName;
                }
            }

            /// 若primaryBeanName为null，则无法确定使用哪个bean
            /// 这里采用全名匹配的方法，选取名称完全相同的bean
            if (primaryBeanName == null
                    && (this.resolvableDependencies.values().contains(beanInstance)
                    || matchesBeanName(candidateBeanName, descriptor.getDependencyName()))) {
                fallbackBeanName = candidateBeanName;
            }
        }

        return (primaryBeanName != null ? primaryBeanName : fallbackBeanName);
    }

    protected boolean isPrimary(String beanName, Object beanInstance) {

        if (containsBeanDefinition(beanName)) {
            return getMergedLocalBeanDefinition(beanName).isPrimary();
        }

        BeanFactory parentFactory = getParentBeanFactory();
        return (parentFactory instanceof DefaultListableBeanFactory &&
                ((DefaultListableBeanFactory) parentFactory).isPrimary(beanName, beanInstance));
    }

    protected boolean matchesBeanName(String beanName, String candidateName) {
        return (candidateName != null &&
                (candidateName.equals(beanName) || ObjectUtils.containsElement(getAliases(beanName), candidateName)));
    }

    private void raiseNoSuchBeanDefinitionException(Class<?> type,
                                                    String dependencyDescription,
                                                    DependencyDescriptor descriptor) {

        throw new NoSuchBeanDefinitionException(type, dependencyDescription,
                "expected at least 1 bean which qualifies as autowire candidate for this dependency. " +
                        "Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
    }


    // ======== BeanFactory interface ========
//    @Override
//    public Object getBean(String name) throws Exception {
//
//        return null;
//    }

//    @Override
//    public <T> T getBean(Class<T> requiredType) throws Exception {
//        return getBean(requiredType, (Object[]) null);
//    }
//
//    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
//        NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args);
//        if (namedBean != null) {
//            return namedBean.getBeanInstance();
//        }
//        BeanFactory parent = getParentBeanFactory();
//        if (parent != null) {
//            return parent.getBean(requiredType, args);
//        }
//        throw new NoSuchBeanDefinitionException(requiredType);
//    }


    // ======== misc ========

    protected ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }

    public AutowireCandidateResolver getAutowireCandidateResolver() {
        return this.autowireCandidateResolver;
    }

    public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
        Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
        this.autowireCandidateResolver = autowireCandidateResolver;
    }


    protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
//        if (this.beanExpressionResolver == null) {
        return value;
//        }
//        Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
//        return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
    }

    /**
     * 根据外层beanName的字段类型和依赖描述元数据创建候选bean
     */
    protected Map<String, Object> findAutowireCandidates(String beanName,
                                                         Class<?> requiredType,
                                                         DependencyDescriptor descriptor) {

        String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                this, requiredType, true, descriptor.isEager());

        // 记录匹配的candidate，如bookDaoMapper -> bean对象，已经实例化
        Map<String, Object> result = new LinkedHashMap<>(candidateNames.length);

        for (Class<?> autowiringType : this.resolvableDependencies.keySet()) {
            if (autowiringType.isAssignableFrom(requiredType)) {
                Object autowiringValue = this.resolvableDependencies.get(autowiringType);

                autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
                if (requiredType.isInstance(autowiringValue)) {
                    result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
                    break;
                }
            }
        }

        for (String candidateName : candidateNames) {
            if (!candidateName.equals(beanName) && isAutowireCandidate(candidateName, descriptor)) {
                // 实例化bean
                Object obj = getBean(candidateName);
                result.put(candidateName, obj);
            }
        }

        return result;
    }

    public BeanFactory getParentBeanFactory() {
//        return this.parentBeanFactory;
        return null;
    }

    /**
     * Serializable ObjectFactory for lazy resolution of a dependency.
     */
    private class DependencyObjectFactory implements ObjectFactory<Object>, Serializable {

        private final DependencyDescriptor descriptor;

        private final String beanName;

        public DependencyObjectFactory(DependencyDescriptor descriptor, String beanName) {
            this.descriptor = new DependencyDescriptor(descriptor);
            this.descriptor.increaseNestingLevel();
            this.beanName = beanName;
        }

        public Object getObject() throws BeansException {
            return doResolveDependency(this.descriptor, this.descriptor.getDependencyType(), this.beanName, null, null);
        }
    }


    /**
     * Serializable ObjectFactory for lazy resolution of a dependency.
     */
    private class DependencyProvider extends DependencyObjectFactory implements Provider<Object> {

        public DependencyProvider(DependencyDescriptor descriptor, String beanName) {
            super(descriptor, beanName);
        }

        public Object get() throws BeansException {
            return getObject();
        }
    }


    /**
     * Separate inner class for avoiding a hard dependency on the {@code javax.inject} API.
     */
    private class DependencyProviderFactory {

        public Object createDependencyProvider(DependencyDescriptor descriptor, String beanName) {
            return new DependencyProvider(descriptor, beanName);
        }
    }

}
