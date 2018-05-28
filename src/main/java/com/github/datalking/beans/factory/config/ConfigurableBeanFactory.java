package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;

/**
 * ConfigurableBeanFactory 接口
 * 主要用于配置BeanFactory
 *
 * @author yaoo on 4/3/18
 */
public interface ConfigurableBeanFactory extends BeanFactory {

    boolean isCurrentlyInCreation(String beanName);

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    int getBeanPostProcessorCount();

    String resolveEmbeddedValue(String value);

    BeanExpressionResolver getBeanExpressionResolver();

    void registerDependentBean(String beanName, String dependentBeanName);

    String[] getDependentBeans(String beanName);

//    String[] getDependenciesForBean(String beanName);

    TypeConverter getTypeConverter();

    void setTypeConverter(TypeConverter typeConverter);


//void destroyBean(String beanName, Object beanInstance);
//void registerAlias(String beanName, String alias) ;

}
