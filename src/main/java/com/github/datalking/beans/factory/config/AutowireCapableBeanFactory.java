package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;

import java.util.Set;

/**
 * AutowireCapableBeanFactory 接口
 * <p>
 * 主要用于创建bean
 *
 * @author yaoo on 4/3/18
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

    /// 依赖注入的方式
    int AUTOWIRE_NO = 0;
    int AUTOWIRE_BY_NAME = 1;
    int AUTOWIRE_BY_TYPE = 2;
    int AUTOWIRE_CONSTRUCTOR = 3;
    int AUTOWIRE_AUTODETECT = 4;


    <T> T createBean(Class<T> beanClass) throws Exception;

    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName);

    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName);

    //    void applyBeanPropertyValues(Object existingBean, String beanName);
//    Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck);
//    void autowireBean(Object existingBean) ;
//    void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck);
//    Object configureBean(Object existingBean, String beanName) ;
//
//
//    void destroyBean(Object existingBean);
    Object initializeBean(Object existingBean, String beanName);
//    <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType);

    Object resolveDependency(DependencyDescriptor descriptor, String beanName);

    Object resolveDependency(DependencyDescriptor descriptor,
                             String beanName,
                             Set<String> autowiredBeanNames,
                             TypeConverter typeConverter);


}
