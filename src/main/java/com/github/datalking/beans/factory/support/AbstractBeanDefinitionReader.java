package com.github.datalking.beans.factory.support;


import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.io.DefaultResourceLoader;
import com.github.datalking.io.ResourceLoader;

/**
 * 从配置中读取BeanDefinition 抽象类
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

    private final BeanDefinitionRegistry registry;

    private ResourceLoader resourceLoader;

//    private Map<String, BeanDefinition> registry;

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.registry = registry;

    }

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.resourceLoader = new DefaultResourceLoader();
    }

    @Deprecated
    public AbstractBeanDefinitionReader(ResourceLoader resourceLoader) {
        this.registry = new DefaultListableBeanFactory();
        this.resourceLoader = new DefaultResourceLoader();
    }


    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }

    public String generateBeanName(BeanDefinition bd) {
        return BeanDefinitionReaderUtils.generateBeanName(bd, this.registry);
    }


}
