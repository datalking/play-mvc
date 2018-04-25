package com.github.datalking.context.support;


import com.github.datalking.beans.factory.config.BeanFactoryPostProcessor;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.support.AbstractBeanFactory;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.DefaultListableBeanFactory;
import com.github.datalking.beans.factory.xml.XmlBeanDefinitionReader;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationContext 抽象类
 */
public abstract class AbstractApplicationContext implements ConfigurableApplicationContext {

    protected DefaultListableBeanFactory beanFactory;

    private String configLocation;

    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

    private String id = ObjectUtils.identityToString(this);

    private String displayName = this.getClass().getName() + "@" + this.hashCode();

    private ApplicationContext parent;

    private boolean active = false;

    private long startupDate;

    public AbstractApplicationContext() {
        // 使用注解，不使用xml时，configLocation默认为空字符串
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

    public AbstractBeanFactory getBeanFactory() {
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
     * <p>
     * 默认采用立即初始化
     */
    @Override
    public void refresh() {

        prepareRefresh();


        try {


            // 读取xml配置文件
            obtainFreshBeanFactory();

            // 执行各种BeanFactoryPostProcessor，如扫描@Configuration、@Bean、@ComponentScan
            invokeBeanFactoryPostProcessors(beanFactory);

            // 注册各种BeanPostProcessor，只注册，真正的调用是在getBean
            // 实例化各种内部bean，如AnnotationAwareAspectJAutoProxyCreator
            registerBeanPostProcessors(beanFactory);

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

    private void obtainFreshBeanFactory() throws Exception {
        if (configLocation != null && !configLocation.trim().equals("")) {
            //读取xml配置并解析成BeanDefinition
            XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(((BeanDefinitionRegistry) getBeanFactory()));
            xmlBeanDefinitionReader.loadBeanDefinitions(configLocation);
        }
    }

    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {

        // beanFactoryPostProcessors默认为空
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

    }

    private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
    }

    private void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) throws Exception {

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

    public void setDisplayName(String displayName) {
        StringUtils.hasLength(displayName);
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
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

    public void setId(String id) {
        this.id = id;
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


}
