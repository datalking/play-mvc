package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanDefinition;

/**
 * @author yaoo on 5/27/18
 */
public interface BeanNameGenerator {

    String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
