package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.common.StringValueResolver;

/**
 * ConfigurableBeanFactory 接口
 * 主要用于配置BeanFactory
 *
 * @author yaoo on 4/3/18
 */
public interface ConfigurableBeanFactory extends BeanFactory {

    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    boolean isCurrentlyInCreation(String beanName);

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    int getBeanPostProcessorCount();

    String resolveEmbeddedValue(String value);

    BeanExpressionResolver getBeanExpressionResolver();

    void registerDependentBean(String beanName, String dependentBeanName);

    String[] getDependentBeans(String beanName);

    String[] getDependenciesForBean(String beanName);

    TypeConverter getTypeConverter();

    void setTypeConverter(TypeConverter typeConverter);

    BeanDefinition getMergedBeanDefinition(String beanName);

    void registerScope(String scopeName, Scope scope);

    void addEmbeddedValueResolver(StringValueResolver valueResolver);

//void destroyBean(String beanName, Object beanInstance);
//void registerAlias(String beanName, String alias) ;

}
