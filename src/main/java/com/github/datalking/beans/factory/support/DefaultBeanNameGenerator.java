package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanDefinition;

/**
 * @author yaoo on 5/28/18
 */
public class DefaultBeanNameGenerator implements BeanNameGenerator {

    public DefaultBeanNameGenerator() {
    }

    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        return BeanDefinitionReaderUtils.generateBeanName(definition, registry);
    }

}
