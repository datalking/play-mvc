package com.github.datalking.context.support;

import com.github.datalking.beans.factory.config.AutowireCapableBeanFactory;
import com.github.datalking.beans.factory.config.BeanFactoryPostProcessor;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.support.BeanDefinitionReader;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.DefaultListableBeanFactory;
import com.github.datalking.beans.factory.xml.XmlBeanDefinitionReader;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.context.MessageSource;
import com.github.datalking.context.MessageSourceResolvable;
import com.github.datalking.context.message.DelegatingMessageSource;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ApplicationContext 抽象类
 */
public abstract class AbstractApplicationContext implements ConfigurableApplicationContext {

    public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

    protected DefaultListableBeanFactory beanFactory;

    private String configLocation;

    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

    private String id = ObjectUtils.identityToString(this);

    private String displayName = this.getClass().getName() + "@" + this.hashCode();

    // parent默认为null
    private ApplicationContext parent;

    private boolean active = false;

    private long startupDate;

    private MessageSource messageSource;

    public AbstractApplicationContext() {
        // 当使用注解而不使用xml时，configLocation默认为空字符串
        this.configLocation = "";
        this.beanFactory = new DefaultListableBeanFactory();

    }

    public AbstractApplicationContext(String configLocation) {
        this.configLocation = configLocation;
        this.beanFactory = new DefaultListableBeanFactory();

    }

    public AbstractApplicationContext(String configLocation, DefaultListableBeanFactory beanFactory) {
        this.configLocation = configLocation;
        this.beanFactory = beanFactory;
    }

    public AbstractApplicationContext(DefaultListableBeanFactory beanFactory) {
        this.configLocation = "";
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public Class<?> getType(String name) {
        return getBeanFactory().getType(name);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> targetType) {
        return getBeanFactory().isTypeMatch(name, targetType);
    }

    /**
     * 读取配置文件并注册bean
     * 默认采用立即初始化
     */
    @Override
    public void refresh() {

        // 暂时用来记录启动时间
        prepareRefresh();

        // 读取xml配置文件和直接输入的java配置
        obtainFreshBeanFactory();

        // beanFactory准备工作，如注册环境变量相关bean和默认beanPostProcessor
        prepareBeanFactory(beanFactory);

        try {

            // 默认为空方法，web环境下会手动注册与ServletContext和ServletConfig实例
            postProcessBeanFactory(beanFactory);

            // 执行各种BeanFactoryPostProcessor，如扫描@Configuration、@Bean、@ComponentScan
            invokeBeanFactoryPostProcessors(beanFactory);

            // 注册各种BeanPostProcessor，只注册，真正的调用是在getBean
            // 实例化各种内部bean，如AspectJAutoProxyCreator
            registerBeanPostProcessors(beanFactory);

            initMessageSource();

            // 通过调用getBean()创建非懒加载而是需要立即实例化的bean
            finishBeanFactoryInitialization(beanFactory);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void prepareRefresh() {

        this.startupDate = System.currentTimeMillis();

        this.active = true;

    }

    protected void obtainFreshBeanFactory() {

        /// 读取xml配置并解析成BeanDefinition
        if (configLocation != null && !configLocation.trim().equals("")) {
            BeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(((BeanDefinitionRegistry) getBeanFactory()));

            try {
                xmlBeanDefinitionReader.loadBeanDefinitions(configLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 读取Java注解配置并解析成BeanDefinition
        loadBeanDefinitions(beanFactory);

    }

    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {

        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

    }


    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {

        // beanFactoryPostProcessors默认为空
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

    }

    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
    }

    protected void initMessageSource() {

//        if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
        if (beanFactory.containsBeanDefinition(MESSAGE_SOURCE_BEAN_NAME)) {
            this.messageSource = (MessageSource) beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME);
        } else {
            DelegatingMessageSource dms = new DelegatingMessageSource();
            this.messageSource = (MessageSource) dms;
            beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
        }
    }

    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) throws Exception {

        //手动调用getBean()方法来触发实例化bean
        beanFactory.preInstantiateSingletons();
    }


    public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return this.beanFactoryPostProcessors;
    }

    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
        Assert.notNull(postProcessor, "BeanFactoryPostProcessor must not be null");
        this.beanFactoryPostProcessors.add(postProcessor);
    }

    public boolean isActive() {
        return this.active;
    }

    public void setParent(ApplicationContext parent) {
        this.parent = parent;
//        if (parent != null) {
//            Environment parentEnvironment = parent.getEnvironment();
//            if (parentEnvironment instanceof ConfigurableEnvironment) {
//                getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
//            }
//        }
    }

    public void close() {
        doClose();
//        synchronized (this.startupShutdownMonitor) {
//            // If we registered a JVM shutdown hook, we don't need it anymore now:
//            // We've already explicitly closed the context.
//            if (this.shutdownHook != null) {
//                try {
//                    Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
//                } catch (IllegalStateException ex) {
//                    // ignore - VM is already shutting down
//                }
//            }
//        }
    }

    private void doClose() {
//        destroyBeans();
//        closeBeanFactory();
//        onClose();
        this.active = false;

    }

    @Override
    public boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    // ======== ApplicationContext interface ========
    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationName() {
        return "";
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        Assert.hasLength(displayName, "Display name must not be empty");
        this.displayName = displayName;
    }

    @Override
    public ApplicationContext getParent() {
        return this.parent;
    }

    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
        return getBeanFactory();
    }

    public long getStartupDate() {
        return this.startupDate;
    }

//    public void publishEvent(ApplicationEvent event) {
//        Assert.notNull(event, "Event must not be null");
//        if (logger.isTraceEnabled()) {
//            logger.trace("Publishing event in " + getDisplayName() + ": " + event);
//        }
//        getApplicationEventMulticaster().multicastEvent(event);
//        if (this.parent != null) {
//            this.parent.publishEvent(event);
//        }
//    }


    // ======== ListableBeanFactory Interface ========
    @Override
    public boolean containsBeanDefinition(String beanName) {
        return getBeanFactory().containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return getBeanFactory().getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);

    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    // ======== MessageSource interface ========

    @Override
    public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
        return getMessageSource().getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object args[], Locale locale) {
        return getMessageSource().getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        return getMessageSource().getMessage(resolvable, locale);
    }

    private MessageSource getMessageSource() throws IllegalStateException {
        if (this.messageSource == null) {
            throw new IllegalStateException("MessageSource not initialized - call 'refresh' before accessing messages via the context: " + this);
        }
        return this.messageSource;
    }

    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory);

}
