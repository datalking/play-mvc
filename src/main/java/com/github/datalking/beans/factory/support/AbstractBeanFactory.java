package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.BeanWrapper;
import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryUtils;
import com.github.datalking.beans.factory.FactoryBean;
import com.github.datalking.beans.factory.ObjectFactory;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.BeanExpressionResolver;
import com.github.datalking.beans.factory.config.BeanPostProcessor;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.beans.factory.config.InstantiationAwareBeanPostProcessor;
import com.github.datalking.beans.factory.config.Scope;
import com.github.datalking.common.StringValueResolver;
import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.common.convert.SimpleTypeConverter;
import com.github.datalking.exception.BeanCurrentlyInCreationException;
import com.github.datalking.exception.BeansException;
import com.github.datalking.exception.NoSuchBeanDefinitionException;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanFactory抽象类
 * getBean(String name)的入口
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    // 存放已经创建或正在创建的bean名称，用于失败回滚或销毁清理
    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));
    // 各类BeanDefinition合并属性后的RootBeanDefinition ConcurrentHashMap
    private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

    private boolean hasInstantiationAwareBeanPostProcessors;

    private final List<StringValueResolver> embeddedValueResolvers = new LinkedList<>();

    private BeanExpressionResolver beanExpressionResolver;

    private ConversionService conversionService;

    private TypeConverter typeConverter;

    private final Map<String, Scope> scopes = new LinkedHashMap<>(8);

    //    private boolean hasDestructionAwareBeanPostProcessors;

    //    private boolean cacheBeanMetadata = true;

    // ======== abstract methods ========

    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args);

    protected abstract BeanDefinition getBeanDefinition(String beanName);

    public abstract boolean containsBeanDefinition(String beanName);

    @Override
    public Class<?> getType(String name) {

        // String beanName = transformedBeanName(name);
        String beanName = name;

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean && !isFactoryDereference(name)) {
                return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
            } else {
                return beanInstance.getClass();
            }
        } else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            return null;
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        Class<?> beanClass = predictBeanType(beanName, mbd);

        if (!isFactoryDereference(name)) {
            return beanClass;
        }

        return null;
    }

    @Override
    public Object getBean(String name) {

        return doGetBean(name, null, null, false);
    }

    protected <T> T doGetBean(final String name,
                              final Class<T> requiredType,
                              final Object[] args,
                              boolean typeCheckOnly) {

        //将别名解析为bean唯一名称
        final String beanName = transformedBeanName(name);
//        final String beanName = name;

        // 最终返回的bean
        Object targetBean;

        /// 从3级缓存中找bean
        Object sharedInstance = getSingleton(beanName);

        /// 如果name对应的bean实例已缓存，则直接返回bean
        if (sharedInstance != null && args == null) {

            // 获取bean实例，处理FactoryBean的入口
            targetBean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        }
        /// 如果name对应的bean实例不存在，则新建bean
        else {

            // 先标记为已经创建
            markBeanAsCreated(beanName);

            final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
            if (mbd == null) {
                throw new NoSuchBeanDefinitionException(beanName + " 对应的BeanDefinition不存在");
            }

            /// 先判断scope为单例，这里默认单例，创建单例bean
            sharedInstance = getSingleton(beanName, (ObjectFactory) () -> createBean(beanName, mbd, args));

            targetBean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
        }


        return (T) targetBean;
    }

    /**
     * 名称处理
     * todo 支持别名解析
     */
    protected String transformedBeanName(String name) {

        String canonicalName = name;

//        String resolvedName;
//        do {
//            resolvedName = this.aliasMap.get(canonicalName);
//            if (resolvedName != null) {
//                canonicalName = resolvedName;
//            }
//        }
//        while (resolvedName != null);

        if (canonicalName.startsWith(FACTORY_BEAN_PREFIX)) {
            canonicalName = canonicalName.substring(1);
        }

        return canonicalName;
    }

    /**
     * 获取bean实例，处理FactoryBean的入口
     */
    protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

        /// 若name以&开头，却不是FactoryBean，则抛出异常
        if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
            throw new BeansException(name + "/" + beanInstance.getClass() + " is NOT a factory ");
        }

        // 若bean不是FactoryBean类型 或 name以&开头，则直接返回bean实例，一般普通bean执行到这里会直接返回
        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {

            return beanInstance;
        }

        /// 若beanInstance为FactoryBean
        Object object = null;
//        if (mbd == null) {
//            object = getCachedObjectForFactoryBean(beanName);
//        }
        if (object == null) {

            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
            /// 根据beanName获得RootBeanDefinition
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }

            // bean是否为合成的，合成bean在获得FactoryBean创建好的bean对象实例后，不需要后置处理
            // 默认false
            boolean synthetic = (mbd != null && mbd.isSynthetic());

            // 使用FactoryBean创建bean实例对象
            object = getObjectFromFactoryBean(factory, beanName, !synthetic);
        }

        return object;
    }

    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {

        /// todo 要支持缓存
        Object object = doGetObjectFromFactoryBean(factory, beanName);
        if (object != null && shouldPostProcess) {
//            object = postProcessObjectFromFactoryBean(object, beanName);
        }

        return object;
    }

    private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, String beanName) {

        Object object;

        // 调用FactoryBean中的getObject()方法创建bean实例
        object = factory.getObject();

        if (object == null && isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName, "FactoryBean which is currently in creation returned null from getObject");
        }

        return object;
    }

    protected void markBeanAsCreated(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            this.alreadyCreated.add(beanName);
        }
    }

    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) {
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null) {
            return mbd;
        }

        // 从beanDefinitionMap里面取
        BeanDefinition bd = getBeanDefinition(beanName);
        return getMergedBeanDefinition(beanName, bd);
    }

    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd) {

        return getMergedBeanDefinition(beanName, bd, null);
    }

    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd, BeanDefinition containingBd) {

        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd;

            if (bd instanceof RootBeanDefinition) {
                mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
            } else {
                mbd = new RootBeanDefinition(bd);
            }
            this.mergedBeanDefinitions.put(beanName, mbd);

            return mbd;
        }

    }

    @Override
    public boolean containsBean(String name) {
        //String beanName = transformedBeanName(name);
        String beanName = name;
        if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
            return true;
        }
        return false;
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");

        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);

        if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
            this.hasInstantiationAwareBeanPostProcessors = true;
        }
//        if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
//            this.hasDestructionAwareBeanPostProcessors = true;
//        }
    }

    @Override
    public int getBeanPostProcessorCount() {
        return this.beanPostProcessors.size();
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    protected boolean hasInstantiationAwareBeanPostProcessors() {
        return this.hasInstantiationAwareBeanPostProcessors;
    }

    /**
     * 判断name名称的bean是否是targetType类型
     */
    @Override
    public boolean isTypeMatch(String name, Class<?> targetType) {

        //String beanName = transformedBeanName(name);
        String beanName = name;
        Class<?> typeToMatch = (targetType != null ? targetType : Object.class);

        // 在 singletonObjects 和 earlySingletonObjects 中查找beanName
        Object beanInstance = getSingleton(beanName, false);

        /// 如果bean不为空
        if (beanInstance != null) {
            /// 如果beanInstance是FactoryBean
            if (beanInstance instanceof FactoryBean) {

                if (!isFactoryDereference(name)) {

                    Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);

                    return (type != null && ClassUtils.isAssignable(typeToMatch, type));
                } else {

                    return ClassUtils.isAssignableValue(typeToMatch, beanInstance);
                }
            }
            /// 如果beanInstance不是FactoryBean，直接比较class
            else {
                return !isFactoryDereference(name) && ClassUtils.isAssignableValue(typeToMatch, beanInstance);
            }
        }

        /// 若bean为空，且beanDefinition不存在
        else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            return false;
        }

        /// 如果bean为空，处理其他情况
        else {

            RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

            Class<?>[] typesToMatch = (FactoryBean.class.equals(typeToMatch) ?
                    new Class<?>[]{typeToMatch} :
                    new Class<?>[]{FactoryBean.class, typeToMatch});

            BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
            if (dbd != null && !isFactoryDereference(name)) {
                RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);

                Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);

                if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
                    return typeToMatch.isAssignableFrom(targetClass);
                }
            }

            //简单地根据 RootBeanDefinition的getBeanClass() 预测bean类型
            Class<?> beanType = predictBeanType(beanName, mbd, typesToMatch);

            if (beanType == null) {
                return false;
            }

            /// 若是普通FactoryBean
            if (FactoryBean.class.isAssignableFrom(beanType)) {
                if (!isFactoryDereference(name)) {
                    beanType = getTypeForFactoryBean(beanName, mbd);
                    if (beanType == null) {
                        return false;
                    }
                }
            }
            /// 若是特殊FactoryBean
            if (isFactoryDereference(name)) {

                beanType = predictBeanType(beanName, mbd, FactoryBean.class);

                if (beanType == null || !FactoryBean.class.isAssignableFrom(beanType)) {
                    return false;
                }
            }

            return typeToMatch.isAssignableFrom(beanType);
        }

    }

    /**
     * 根据RootBeanDefinition的getBeanClass()预测bean类型
     * 只是简单的判断，被子类覆盖
     */
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {

        Class<?> targetType = mbd.getTargetType();
        if (targetType != null) {
            return targetType;
        }

        ///
        if (mbd.getFactoryMethodName() != null) {
            return null;
        }

        return resolveBeanClass(mbd, beanName, typesToMatch);
    }

    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch) {
        if (mbd.hasBeanClass()) {
            return mbd.getBeanClass();
        }
        return doResolveBeanClass(mbd, typesToMatch);
    }

    private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch) {

        if (typesToMatch != null && typesToMatch.length > 0) {

            Class clazz = null;

            for (Class c : typesToMatch) {
                try {
                    clazz = Class.forName(c.getName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            String className = mbd.getBeanClassName();
            return (className != null ? clazz : null);
        }

        return null;
    }

    // 根据name名称前缀是否是&判断是否是FactoryBean
    public boolean isFactoryDereference(String name) {

        return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
    }

    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {

        return factoryBean.getObjectType();
    }

    protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
        if (!mbd.isSingleton()) {
            return null;
        }

        FactoryBean<?> factoryBean = null;
        try {
            factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return getTypeForFactoryBean(factoryBean);

    }

    /**
     * 直接根据bean实例判断
     * 只判断简单的情况
     */
    public boolean isFactoryBean(String name) {

        String beanName = transformedBeanName(name);
//        String beanName = name;
        Object beanInstance = getSingleton(beanName, false);

        /// 直接根据bean实例的类型判断
        if (beanInstance != null) {
            return (beanInstance instanceof FactoryBean);
        }

        return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
    }

    /**
     * 根据RootBeanDefinition的信息判断
     */
    protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {

        Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);

//        /// 若beanClass非空且是FactoryBean的子类
//        if (mbd.hasBeanClass()) {
//            if (FactoryBean.class.isAssignableFrom(mbd.getBeanClass())) {
//                return true;
//            }
//        }
//
//        /// 若beanClass不存在，且存在FactoryMethodName，则不为FactoryBean
//        if (mbd.getFactoryMethodName() != null) {
//            return false;
//        }
//
//        if (mbd.getFactoryBeanName() != null && mbd.getFactoryMethodName() != null) {
//            return true;
//        } else if (!mbd.getConstructorArgumentValues().isEmpty()) {
//            return true;
//        }

        return (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
//        return false;
    }

    public String resolveEmbeddedValue(String value) {
        String result = value;
        for (StringValueResolver resolver : this.embeddedValueResolvers) {
            if (result == null) {
                return null;
            }
            result = resolver.resolveStringValue(result);
        }
        return result;
    }

    public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
        Assert.notNull(valueResolver, "StringValueResolver must not be null");
        this.embeddedValueResolvers.add(valueResolver);
    }

    public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
        this.beanExpressionResolver = resolver;
    }

    public BeanExpressionResolver getBeanExpressionResolver() {
        return this.beanExpressionResolver;
    }

    public TypeConverter getTypeConverter() {
//        TypeConverter customConverter = getCustomTypeConverter();
//        if (customConverter != null) {
//            return customConverter;
//        } else {
        // Build default TypeConverter, registering custom editors.
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        typeConverter.setConversionService(getConversionService());
//            registerCustomEditors(typeConverter);
        return typeConverter;
//        }
    }

    public ConversionService getConversionService() {
        return this.conversionService;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    public BeanDefinition getMergedBeanDefinition(String name) {
//        String beanName = transformedBeanName(name);
        String beanName = name;

        // Efficiently check whether bean definition exists in this factory.
//        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
//            return ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName);
//        }
        // Resolve merged bean definition locally.
        return getMergedLocalBeanDefinition(beanName);
    }

    protected void initBeanWrapper(BeanWrapper bw) {
        bw.setConversionService(getConversionService());
//        registerCustomEditors(bw);
    }

    protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
//        if (this.beanExpressionResolver == null) {
        return value;
//        }
//        Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
//        return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
    }

    @Override
    public void registerScope(String scopeName, Scope scope) {
        Assert.notNull(scopeName, "Scope identifier must not be null");
        Assert.notNull(scope, "Scope must not be null");
        if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
            throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
        }
        this.scopes.put(scopeName, scope);

    }


//    public void destroyBean(String beanName, Object beanInstance) {
//    protected void initBeanWrapper(BeanWrapper bw) {

//    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
//    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {


}
