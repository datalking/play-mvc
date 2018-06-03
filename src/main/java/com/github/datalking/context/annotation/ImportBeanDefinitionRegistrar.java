package com.github.datalking.context.annotation;

import com.github.datalking.common.meta.AnnotationMetadata;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author yaoo on 4/17/18
 */
public interface ImportBeanDefinitionRegistrar {

    // 通过@Import注解以Java代码编程方式注册额外的BeanDefinition，如@EnableWebMvc
    void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);

}
