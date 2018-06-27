package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.BeanWrapper;
import com.github.datalking.beans.BeanWrapperImpl;
import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.beans.PropertyValue;
import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.Aware;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.BeanNameAware;
import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.beans.factory.config.AutowireByTypeDependencyDescriptor;
import com.github.datalking.beans.factory.config.AutowireCapableBeanFactory;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanPostProcessor;
import com.github.datalking.beans.factory.config.DependencyDescriptor;
import com.github.datalking.beans.factory.config.InstantiationAwareBeanPostProcessor;
import com.github.datalking.common.LocalVariableTableParameterNameDiscoverer;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.common.PriorityOrdered;
import com.github.datalking.exception.BeansException;
import com.github.datalking.exception.UnsatisfiedDependencyException;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.MethodUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * BeanFactory抽象类
 * 实际创建bean、装配属性
 *
 * @author yaoo on 4/3/18
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
        implements AutowireCapableBeanFactory {

    private boolean allowCircularReferences = true;

    public ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    //    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();
    private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

    public AbstractAutowireCapableBeanFactory() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createBean(Class<T> beanClass) {
        BeanDefinition bd = new RootBeanDefinition(beanClass);
        return (T) createBean(beanClass.getName(), (RootBeanDefinition) bd, null);
    }

    @Override
    protected Object createBean(String beanName, RootBeanDefinition bd, Object[] args) {

        Class beanClass = doResolveBeanClass(bd);
        // ConfigurationClassBD的beanClass是null，FactoryBean的beanClass也可能是null
        if (beanClass != null) {
            bd.setBeanClass(beanClass);
        }

        // ==== 执行实例化前置处理器，即BeanPostProcessor的postProcessBeforeInstantiation()和postProcessAfterInitialization()
        Object bean = resolveBeforeInstantiation(beanName, bd);
        // 如果生成的代理对象不为空，则直接返回
        if (bean != null) {
            return bean;
        }

        /// ==== 如果没有生成代理对象，就按正常流程走，生成Bean对象
        Object beanInstance = doCreateBean(beanName, bd, args);

        return beanInstance;
    }


    protected Object doCreateBean(final String beanName, final RootBeanDefinition bd, final Object[] args) {

        BeanWrapper instanceWrapper = null;

        if (bd.isSingleton()) {
//            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }

        if (instanceWrapper == null) {
            //==== 默认调用无参构造函数新建bean实例，也可以调用工厂方法，尚未注入属性
            instanceWrapper = createBeanInstance(beanName, bd, args);
        }

        final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);

        if (!bd.postProcessed) {
            // 构建autowire需要的元数据添加到缓存，只查找需要注入的字段，并添加到externallyManagedConfigMembers
            applyMergedBeanDefinitionPostProcessors(bd, bean.getClass(), beanName);
            bd.postProcessed = true;
        }

        boolean earlySingletonExposure = (bd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            // 先添加到singletonFactories和registeredSingletons中，提前曝光引用
            addSingletonFactory(beanName, () -> bean);
        }

        //==== 注入字段属性，包括autowire依赖的bean
        populateBean(beanName, bd, instanceWrapper);

        Object exposedObject = bean;
        if (exposedObject != null) {
            // sqlsessionFactoryBean会创建DefaultSqlSessionFactory实例
            //==== 调用 postProcessBeforeInitialization > afterPropertiesSet > postProcessAfterInitialization
            exposedObject = initializeBean(beanName, exposedObject, bd);
        }

        //注册bean销毁的方法
//      registerDisposableBeanIfNecessary(beanName, bean, mbd);

        return exposedObject;
    }

    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {

        Class beanClass = doResolveBeanClass(mbd);
//        bd.setBeanClass(beanClass);


        /// 根据ConfigurationClassBeanDefinition指定的FactoryMethod创建bean实例
        if (mbd.getFactoryMethodName() != null) {
            return instantiateUsingFactoryMethod(beanName, mbd, args);
        }

        /// 根据参数匹配构造方法，并创建bean实例 todo 场景是
        if (!mbd.getConstructorArgumentValues().isEmpty()) {
//            return autowireConstructor(beanName, mbd, ctors, args);
            return autowireConstructor(beanName, mbd, null, args);
        }

        /// 直接使用无参构造函数创建对象
        return instantiateBean(beanName, mbd);

    }

    protected BeanWrapper autowireConstructor(String beanName,
                                              RootBeanDefinition mbd,
                                              Constructor<?>[] ctors,
                                              Object[] explicitArgs) {

        ConstructorResolver resolver = new ConstructorResolver(this);
        return resolver.autowireConstructor(beanName, mbd, ctors, explicitArgs);
    }

    /**
     * 通过jdk反射生成bean实例
     * spring对调用无参构造函数生成实例使用的是cglib
     */
    private BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition bd) {

        Object beanInstance = null;
        try {
            beanInstance = bd.getBeanClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        BeanWrapper bw = new BeanWrapperImpl(beanInstance);

        return bw;

    }

    /**
     * 将BeanDefinition的属性注入bean，注入普通字段和@Autowired、@Value的字段
     * autowire的入口
     */
    private void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
        PropertyValues pvs = mbd.getPropertyValues();

        if (bw == null) {
            return;
        }

        // 若为0，则未使用依赖注入；若为1，则by name；若为2，则by type
        // 默认为0，不会执行if中的语句
        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {

            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, bw, newPvs);
            }

            // by type，mybatis的MapperFactoryBean默认进这里
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, bw, newPvs);
            }

            pvs = newPvs;
        }

        boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
//        boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
        boolean needsDepCheck = false;

        if (hasInstAwareBpps || needsDepCheck) {
//            PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
            PropertyDescriptor[] filteredPds = null;

            if (hasInstAwareBpps) {
                List<BeanPostProcessor> bpps = getBeanPostProcessors();
                for (BeanPostProcessor bp : bpps) {
                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
                        InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;

                        // 下面会通过AutowiredAnnotationBeanPostProcessor的方法自动注入依赖的bean，直接从缓存中取autowire的字段
                        // 解析@Value中属性占位符的入口
                        Object obj = bw.getWrappedInstance();
                        pvs = ibp.postProcessPropertyValues(pvs, filteredPds, obj, beanName);
                        if (pvs == null) {
                            return;
                        }
                    }
                }
            }
            if (needsDepCheck) {
//                checkDependencies(beanName, mbd, filteredPds, pvs);
            }
        }

        // 将BeanDefinition的属性注入到bean实例
        applyPropertyValues(beanName, mbd, bw, pvs);
    }

    /**
     * 构建autowire的元数据添加到缓存
     */
    protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
        List<BeanPostProcessor> bpps = getBeanPostProcessors();
        for (BeanPostProcessor bp : bpps) {

            if (bp instanceof MergedBeanDefinitionPostProcessor) {
                MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
                bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
            }
        }
    }

    protected void autowireByName(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);

        for (String propertyName : propertyNames) {
            if (containsBean(propertyName)) {

                Object bean = getBean(propertyName);
                pvs.add(propertyName, bean);
                registerDependentBean(propertyName, beanName);

            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
                            "' by name: no matching bean found");
                }
            }
        }
    }

    /**
     * 按类型注入bean的属性
     * 如处理sqlSessionFactory
     */
    protected void autowireByType(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

//        TypeConverter converter = getCustomTypeConverter();
        TypeConverter converter = null;
        if (converter == null) {
            converter = bw;
        }

        // 记录该依赖项所依赖的其他bean
        Set<String> autowiredBeanNames = new LinkedHashSet<>(4);

        // 获取bean非基本类型和非字符串类型的属性，可以是set方法的参数，不能是ref类型字段
        // todo 只处理了mybatis
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);

        Class<?> beanClass = mbd.getBeanClass();
        Field[] fields = beanClass.getDeclaredFields();
        Set<Method> setMethods = MethodUtils.getSetMethodsIncludingParent(beanClass);

        for (String propertyName : propertyNames) {
            try {
                Class<?> pType = null;
                Method setMethod = null;

                /// 获取该属性对应的字段
//                PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
                for (Field f : fields) {
                    if (f.getName().equals(propertyName)) {
                        pType = f.getType();
                    }
                }

                /// 若该属性不是字段，则尝试获取方法 todo 只处理了单参数
                if (pType == null) {
                    for (Method m : setMethods) {
                        if (propertyName.equals(StringUtils.getBeanNameFromSetMethod(m.getName()))) {
                            setMethod = m;
                            pType = m.getParameterTypes()[0];
                        }
                    }
                }

                // 不处理Object类型的注入，没意义
//                if (Object.class != pd.getPropertyType()) {
                if (Object.class != pType) {

                    // 获取该属性对应的方法参数
//                    MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                    MethodParameter methodParam = new MethodParameter(setMethod, 0);
                    // Do not allow eager init for type matching in case of a prioritized post-processor.
                    boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());

                    DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);

                    // todo 解析sqlSessionFactory时，得到的是DefaultSqlSessionFactory实例；解析sqlSessionTemplete得到的是null
                    // 获取代表属性值的对象，可能包含getBean()
                    Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);

                    if (autowiredArgument != null) {
                        pvs.add(propertyName, autowiredArgument);
                    }

                    for (String autowiredBeanName : autowiredBeanNames) {
                        registerDependentBean(autowiredBeanName, beanName);
                    }

                    autowiredBeanNames.clear();
                }
            } catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(mbd.getBeanClassName(), beanName, propertyName, ex);
            }
        }
    }


    /**
     * 获取bean非基本类型和非字符串类型的属性
     * todo 只处理了mybatis dao的set方法
     */
    protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
        Set<String> result = new TreeSet<>();
        Class<?> beanClass = bw.getWrappedClass();

        Set<Method> setMehtods = MethodUtils.getSetMethodsIncludingParent(beanClass);

        for (Method m : setMehtods) {
            String mName = m.getName();
            result.add(StringUtils.getBeanNameFromSetMethod(mName));
        }

        Set<String> rm = new HashSet<>();
        /// 去掉字段
        Field[] fields = beanClass.getDeclaredFields();
        for (String s : result) {

            for (Field f : fields) {
                if (f.getName().equals(s)) {
                    rm.add(s);
                }
            }

        }

        result.removeAll(rm);


//        PropertyValues pvs = mbd.getPropertyValues();
//        PropertyDescriptor[] pds = bw.getPropertyDescriptors();
//        for (PropertyDescriptor pd : pds) {
//            if (pd.getWriteMethod() != null
//                    && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName())
//                    && !BeanUtils.isSimpleProperty(pd.getPropertyType())) {
//
//                result.add(pd.getName());
//            }
//        }

        return StringUtils.toStringArray(result);
    }

    /**
     * 使用工厂方法创建bean实例
     *
     * @param beanName     要创建的bean的名称
     * @param bd           该bean的BeanDefiniiton
     * @param explicitArgs 参数
     * @return bean实例包装类
     */
    private BeanWrapper instantiateUsingFactoryMethod(final String beanName, final RootBeanDefinition bd, final Object[] explicitArgs) {

        BeanWrapperImpl bw = new BeanWrapperImpl();

        Object factoryBean = null;
        String factoryBeanName = bd.getFactoryBeanName();
        Class<?> factoryClass;
        //boolean isStatic;

        try {
            //==== 实例化外层Bean
            factoryBean = getBean(factoryBeanName);
            if (factoryBean == null) {
                throw new Exception(beanName + "找不到FactoryBeanName");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        factoryClass = factoryBean.getClass();

        /// 查找最接近的方法
        Method factoryMethodToUse = null;
        //ArgumentsHolder argsHolderToUse = null;
        //Object[] argsToUse = null;

//        Method[] maybeFactoryMethods = factoryClass.getDeclaredMethods();
        Method[] maybeFactoryMethods = factoryClass.getMethods();
        for (Method m : maybeFactoryMethods) {
            if (m.getName().equals(beanName)) {
                factoryMethodToUse = m;
            }
        }

        if (factoryMethodToUse == null) {
            try {
                throw new Exception(beanName + "找不到factoryMethodToUse");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 目标bean实例
        Object beanInstance = null;

        // 获取工厂方法的参数 todo 处理工厂方法的参数，一般也是解析并getBean()
        Class<?>[] types = factoryMethodToUse.getParameterTypes();

        /// 若方法没有参数，则直接调用此工厂方法
        if (types != null && types.length == 0) {
            try {

//            beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, this.beanFactory, factoryBean, factoryMethodToUse, argsToUse);
                beanInstance = factoryMethodToUse.invoke(factoryBean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        /// 若方法存在参数，则解析方法参数，并调用此工厂方法 todo 根据参数名称getBean()，这里默认根据参数type实例化
        else {
            // 存储所有实参对象
            List<Object> args = new ArrayList<>();

            /// 获取所有参数的类型
            for (Class<?> c : types) {

                String classToBeanName = ClassUtils.getCamelCaseNameFromClass(c);

                /// 若方法参数类型对应的BeanDefinition存在，则直接getBean()
                if (getBeanNamesForType(c).length > 0) {
                    // todo 查找最匹配的bean
                    String bName = getBeanNamesForType(c)[0];
                    Object obj = getBean(bName);
                    args.add(obj);
                }
                /// 若方法参数类型对应的BeanDefinition存在，则直接getBean()
                else if (containsBeanDefinition(classToBeanName)) {
//                    beanFactory.resolveDependency(new DependencyDescriptor(param, true), beanName, autowiredBeanNames, typeConverter);
                    Object obj = getBean(classToBeanName);
                    args.add(obj);
                }
//                else if (containsBeanDefinition(paramName))
                /// 若方法参数对应的bean无法解析，则抛出异常
                else {
                    try {
                        throw new Exception(beanName + "使用工厂方法创建时，输入参数无法解析");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!args.isEmpty()) {
                try {
                    beanInstance = factoryMethodToUse.invoke(factoryBean, args.toArray());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }

        if (beanInstance == null) {
            return null;
        }

        bw.setBeanInstance(beanInstance);
        return bw;
    }

    /**
     * 将BeanDefinition的属性注入到bean实例
     *
     * @param beanName 待添加属性的beanName
     * @param bd       要添加的属性定义
     * @param bw       beanWrapper实例
     */
    protected void applyPropertyValues(String beanName, BeanDefinition bd, BeanWrapper bw, PropertyValues pvs) {

        List<PropertyValue> pvList = bd.getPropertyValues().getPropertyValueList();

        MutablePropertyValues mpvs = null;
        if (pvs instanceof MutablePropertyValues) {
            mpvs = (MutablePropertyValues) pvs;
        }

        if (mpvs != null) {
            pvList = mpvs.getPropertyValueList();
        }

        List<PropertyValue> deepCopy = new ArrayList<>(pvList.size());

        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);

        for (PropertyValue pv : pvList) {

            String pvName = pv.getName();
            Object pvValue = pv.getValue();

            // ==== 属性值类型转换
            // 默认转换规则：循环ref转bean实例，string直接返回值
            Object resolvedValue = null;
            resolvedValue = valueResolver.resolveValueIfNecessary(pv, pvValue);

            deepCopy.add(new PropertyValue(pvName, resolvedValue));
        }

        // ==== 批量设置值
        bw.setPropertyValues(new MutablePropertyValues(deepCopy));

    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) {
        return initializeBean(beanName, existingBean, null);
    }


    /**
     * bean实例化且注入属性后执行的方法
     * 执行 Aware的setXxx() -> postProcessBeforeInitialization() -> afterPropertiesSet() -> postProcessAfterInitialization()
     * <p>
     * 具体任务有注入DefaultSqlSessionFactory、TransactionFactory
     */
    protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {

        // 执行最基本的aware方法，包括设置beanName、BeanFactory
        invokeAwareMethods(beanName, bean);

        Object wrappedBean = bean;

        // 执行 初始化前 处理器
        // 执行用户定义的Aware的方法，包括注入environment、ApplicationContext、servletContext
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }
        // 执行 afterPropertiesSet()
        invokeInitMethods(beanName, wrappedBean, mbd);

        // 依次调用所有 初始化后 处理器
        // 包括生成代理对象
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }

        // 如果启用了AOP此处应该返回了代理对象，原来初始化的bean被替换了
        return wrappedBean;
    }


    private void invokeAwareMethods(final String beanName, final Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
//            if (bean instanceof BeanClassLoaderAware) {
//                ((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
//            }
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) {
        Object result = existingBean;
        List<BeanPostProcessor> bppList = getBeanPostProcessors();
        for (BeanPostProcessor bpp : bppList) {

            result = bpp.postProcessBeforeInitialization(result, beanName);

            if (result == null) {
                return result;
            }
        }
        return result;
    }

    protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd) {
        boolean isInitializingBean = bean instanceof InitializingBean;
        if (isInitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

//        if (mbd != null) {
//            String initMethodName = mbd.getInitMethodName();
//            if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
//                    !mbd.isExternallyManagedInitMethod(initMethodName)) {
//                invokeCustomInitMethod(beanName, bean, mbd);
//            }
//        }
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {

        Object result = existingBean;
        List<BeanPostProcessor> bppList = getBeanPostProcessors();

        for (BeanPostProcessor bpp : bppList) {

            // 执行BeanPostProcessor，可以生成的代理对象
            result = bpp.postProcessAfterInitialization(result, beanName);

            if (result == null) {
                return result;
            }
        }
        return result;
    }

    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {

        List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessors();

        for (BeanPostProcessor bp : beanPostProcessors) {

            if (bp instanceof InstantiationAwareBeanPostProcessor) {

                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;

                // 生成代理对象
                Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {

            if (mbd.hasBeanClass() && hasInstantiationAwareBeanPostProcessors()) {

                bean = applyBeanPostProcessorsBeforeInstantiation(mbd.getBeanClass(), beanName);
                if (bean != null) {
                    bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                }
            }
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
    }

    public Class<?> doResolveBeanClass(RootBeanDefinition bd) {
        return doResolveBeanClass((AbstractBeanDefinition) bd);
    }

    public Class<?> doResolveBeanClass(AbstractBeanDefinition bd) {
        String className = bd.getBeanClassName();
        if (className != null) {

            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    /**
     * 预测BeanDefinition所代表的bean的类型
     */
    @Override
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);

//        if (targetType != null  && hasInstantiationAwareBeanPostProcessors()) {
//            for (BeanPostProcessor bp : getBeanPostProcessors()) {
//                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
//                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
//                    Class<?> predicted = ibp.predictBeanType(targetType, beanName);
//                    if (predicted != null && (typesToMatch.length != 1 || FactoryBean.class != typesToMatch[0] ||
//                            FactoryBean.class.isAssignableFrom(predicted))) {
//                        return predicted;
//                    }
//                }
//            }
//        }
        return targetType;
    }

    protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();

        if (targetType == null) {
            targetType = (mbd.getFactoryMethodName() != null ?
                    getTypeForFactoryMethod(beanName, mbd, typesToMatch) :
                    resolveBeanClass(mbd, beanName, typesToMatch));

            if (ObjectUtils.isEmpty(typesToMatch)) {
                mbd.setTargetType(targetType);
            }
        }

        return targetType;
    }

    protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class[] typesToMatch) {

        Class<?> factoryClass;
        boolean isStatic = true;

        String factoryBeanName = mbd.getFactoryBeanName();
        if (factoryBeanName != null) {
            factoryClass = getType(factoryBeanName);
            isStatic = false;
        } else {
            factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
        }

        if (factoryClass == null) {
            return null;
        }

        // 存储返回值类型
        Class<?> commonType = null;
        if (mbd instanceof ConfigurationClassBeanDefinition) {
            String returnTypeName = ((ConfigurationClassBeanDefinition) mbd).getFactoryMethodMetadata().getReturnTypeName();

            try {
                commonType = Class.forName(returnTypeName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (commonType != null) {
            return commonType;
        }

        return null;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    protected ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }

    protected InstantiationStrategy getInstantiationStrategy() {
        return this.instantiationStrategy;
    }


    public abstract String[] getBeanNamesForType(Class<?> type);
//public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
//public void autowireBean(Object existingBean) {

}
