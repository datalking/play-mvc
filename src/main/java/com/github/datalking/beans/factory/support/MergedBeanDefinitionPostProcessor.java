package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanPostProcessor;

/**
 * @author yaoo on 5/28/18
 */
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

    void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
                                         Class<?> beanType,
                                         String beanName);

}
