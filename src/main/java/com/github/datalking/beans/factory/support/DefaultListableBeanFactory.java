package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryUtils;
import com.github.datalking.beans.factory.FactoryBean;
import com.github.datalking.beans.factory.ObjectFactory;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
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

    private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();

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

        // 遍历的是副本，此时仍然可以beanDefinition
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

        for (String beanName : beanNames) {

            if (beanName.equals("requestMappingHandlerMapping")) {
                System.out.println("====preInstantiateSingletons: " + beanName);
            }

            getBean(beanName);
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
        return getAutowireCandidateResolver().isAutowireCandidate(
                new BeanDefinitionHolder(mbd, beanName, getAliases(beanName)), descriptor);
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
     * 获取指定class类型的bean名称
     * 包括扫描的bean
     */
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();

        for (String beanName : this.beanDefinitionNames) {

            // 各种BeanDefinition添加进 mergedBeanDefinitions
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);

            /// 若是普通bean
            if (bd.hasBeanClass()) {
                if (type.isAssignableFrom(bd.getBeanClass())) {
                    result.add(beanName);
                }
            }
            /// 若是FactoryBean
            else {

                if (bd.getFactoryMethodName() != null) {
                    if (bd instanceof ConfigurationClassBeanDefinition) {
                        String returnTypeName = ((ConfigurationClassBeanDefinition) bd).getFactoryMethodMetadata().getReturnTypeName();
                        Class c = null;
                        try {
                            c = Class.forName(returnTypeName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (type.isAssignableFrom(c)) {
                            result.add(beanName);
                        }
                    }
                }

            }

        }

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

            /// 可能返回的是代理对象，如mybatis的mapper
            return doResolveDependency(descriptor, descriptor.getDependencyType(), beanName, autowiredBeanNames, typeConverter);
        }
    }

    protected Object doResolveDependency(DependencyDescriptor descriptor,
                                         Class<?> type,
                                         String beanName,
                                         Set<String> autowiredBeanNames,
                                         TypeConverter typeConverter) {

        // 获取@Value注解的值，可能含有占位符
        Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);

        if (value != null) {
            if (value instanceof String) {

                // 使用 PropertySourcesPlaceholderConfigurer 解析占位符
                String strVal = resolveEmbeddedValue((String) value);
                BeanDefinition bd = (beanName != null && containsBean(beanName) ? getMergedBeanDefinition(beanName) : null);
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
        /// 若type是普通Object
        else {

            // ==== 寻找依赖bean的候选项，保存到matchingBeans
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
            Object obj = (instanceCandidate instanceof Class ?
                    descriptor.resolveCandidate(autowiredBeanName, type, this) : instanceCandidate);

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

    protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
//        if (this.beanExpressionResolver == null) {
        return value;
//        }
//        Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
//        return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
    }

    protected Map<String, Object> findAutowireCandidates(String beanName,
                                                         Class<?> requiredType,
                                                         DependencyDescriptor descriptor) {

        String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                this, requiredType, true, descriptor.isEager());

        // 记录匹配的candidate，如bookDaoMapper -> class对象
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
                result.put(candidateName, getBean(candidateName));
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
