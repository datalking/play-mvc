package com.github.datalking.context.annotation;

import com.github.datalking.common.meta.AnnotationMetadata;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author yaoo on 4/17/18
 */
public interface ImportBeanDefinitionRegistrar {

    void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);

}
