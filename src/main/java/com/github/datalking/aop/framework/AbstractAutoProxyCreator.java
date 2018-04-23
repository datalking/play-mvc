package com.github.datalking.aop.framework;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.Pointcut;
import com.github.datalking.aop.SingletonTargetSource;
import com.github.datalking.aop.TargetSource;
import com.github.datalking.aop.framework.adapter.AdvisorAdapterRegistry;
import com.github.datalking.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.FactoryBean;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import com.github.datalking.common.Ordered;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;
import org.aopalliance.aop.Advice;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建代理对象 抽象类
 *
 * @author yaoo on 4/18/18
 */
public abstract class AbstractAutoProxyCreator extends ProxyConfig
        implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware, Ordered {

    // 不使用代理的标志
    protected static final Object[] DO_NOT_PROXY = null;
    // 无增强方法的代理对象的标识
    protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];

    private BeanFactory beanFactory;
    // 获取单例对象，封装advice为advisor
    private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

    // 拦截器，默认为空
    private String[] interceptorNames = new String[0];

    private int order = Ordered.LOWEST_PRECEDENCE;

    // 在postProcessBeforeInstantiation()中成功创建的代理对象都会将beanName加入到targetSourceBeans中
    private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    // 作为缓存，存储已经创建代理后的bean和无需代理的bean
    private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);

    protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource);

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    public void setInterceptorNames(String... interceptorNames) {
        this.interceptorNames = interceptorNames;
    }

    public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
        this.advisorAdapterRegistry = advisorAdapterRegistry;
    }


    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {

        Object cacheKey = getCacheKey(beanClass, beanName);

        if (beanName == null || !this.targetSourcedBeans.contains(beanName)) {
            if (this.advisedBeans.containsKey(cacheKey)) {
                return null;
            }

            /// 先判断要不要跳过此beanClass，调用AnnotationAwareAspectJAutoProxyCreator的findCandidateAdvisors()扫描aspectBeanNames
            if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
                this.advisedBeans.put(cacheKey, Boolean.FALSE);
                return null;
            }
        }

        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 默认创建代理对象的方法
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if (bean != null) {
            // 根据给定的 bean 的 class 和 name 构建出个 key，格式：beanClassName_beanName
            Object cacheKey = getCacheKey(bean.getClass(), beanName);

            //if (!this.earlyProxyReferences.contains(cacheKey)) {

            // 如果它适合被代理，则需要封装指定 bean
            return wrapIfNecessary(bean, beanName, cacheKey);
            //}
        }
        return bean;
    }

    /**
     * 直接以beanClass作为缓存
     */
    protected Object getCacheKey(Class<?> beanClass, String beanName) {
        if (StringUtils.hasLength(beanName)) {
            return (FactoryBean.class.isAssignableFrom(beanClass) ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName);
        } else {
            return beanClass;
        }
    }

    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {

        // 是否已经处理过
//        if (beanName != null && this.targetSourcedBeans.contains(beanName)) {
//            return bean;
//        }

        // 无需增强
        if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
            return bean;
        }

        // 给定的bean类是否代表一个基础设施类，基础设施类不应代理，或者配置了指定bean不需要自动代理
        // shouldKip()默认返回false
        if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return bean;
        }

        // 判断当前bean是否需要进行代理，若需要，则返回满足条件的Advice或者Advisor集合，取得Bean相关的Interceptor
        Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);

        // 如果获取到了增强则需要针对增强创建代理
        if (specificInterceptors != DO_NOT_PROXY) {
            this.advisedBeans.put(cacheKey, Boolean.TRUE);

            //==== 创建代理
            Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
            //this.proxyTypes.put(cacheKey, proxy.getClass());

            return proxy;
        }

        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }


    protected Object createProxy(Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {

//        if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
//            AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
//        }

        ProxyFactory proxyFactory = new ProxyFactory();
        //拷贝当前类中的相关属性
        //proxyFactory.copyFrom(this);

        //判定是否代理beanClass
        if (!proxyFactory.isProxyTargetClass()) {
            if (shouldProxyTargetClass(beanClass, beanName)) {
                proxyFactory.setProxyTargetClass(true);
            } else {
                // 获取beanClass实现的接口
                evaluateProxyInterfaces(beanClass, proxyFactory);
            }
        }

        // 添加Advisors
        Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
        for (Advisor advisor : advisors) {
            proxyFactory.addAdvisor(advisor);
        }

        // 设置目标类
        proxyFactory.setTargetSource(targetSource);
        // 定制代理
        //customizeProxyFactory(proxyFactory);
        // 设置是否冻结，默认为false即代理设置后不允许修改代理的配置
        //proxyFactory.setFrozen(this.freezeProxy);
//        if (advisorsPreFiltered()) {
//            proxyFactory.setPreFiltered(true);
//        }

        return proxyFactory.getProxy();
    }

    protected boolean shouldProxyTargetClass(Class<?> beanClass, String beanName) {
        return false;
    }

    /**
     * 检查beanClass的接口，并存储到proxyFactory
     */
    protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
        Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, this.getClass().getClassLoader());

//        boolean hasReasonableProxyInterface = false;
//        for (Class<?> ifc : targetInterfaces) {
//            if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) &&
//                    ifc.getMethods().length > 0) {
//                hasReasonableProxyInterface = true;
//                break;
//            }
//        }
//        if (hasReasonableProxyInterface) {
        for (Class<?> ifc : targetInterfaces) {
            proxyFactory.addInterface(ifc);
        }
//        } else {
//            proxyFactory.setProxyTargetClass(true);
//        }
    }

    /**
     * 获取bean的advisor
     */
    private Advisor[] buildAdvisors(String beanName, Object[] specificInterceptors) {

        Advisor[] commonInterceptors = resolveInterceptorNames();

        List<Object> allInterceptors = new ArrayList<>();
        if (specificInterceptors != null) {
            allInterceptors.addAll(Arrays.asList(specificInterceptors));
            if (commonInterceptors.length > 0) {
                // 默认添加公共拦截器到列表头部
                allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
            }
        }

        Advisor[] advisors = new Advisor[allInterceptors.size()];
        for (int i = 0; i < allInterceptors.size(); i++) {
            advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
        }
        return advisors;
    }

    /**
     * 解析拦截器名到advisor
     */
    private Advisor[] resolveInterceptorNames() {
        ConfigurableBeanFactory cbf = (this.beanFactory instanceof ConfigurableBeanFactory ?
                (ConfigurableBeanFactory) this.beanFactory : null);
        List<Advisor> advisors = new ArrayList<>();
        for (String beanName : this.interceptorNames) {
            if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {

                Object next = this.beanFactory.getBean(beanName);

                advisors.add(this.advisorAdapterRegistry.wrap(next));
            }
        }
        return advisors.toArray(new Advisor[advisors.size()]);
    }


    protected boolean isInfrastructureClass(Class<?> beanClass) {
        boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
                Pointcut.class.isAssignableFrom(beanClass) ||
                Advisor.class.isAssignableFrom(beanClass);
        return retVal;
    }

    protected boolean shouldSkip(Class<?> beanClass, String beanName) {
        return false;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}


