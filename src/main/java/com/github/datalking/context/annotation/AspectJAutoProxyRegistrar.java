package com.github.datalking.context.annotation;

import com.github.datalking.annotation.meta.AnnotationMetadata;
import com.github.datalking.aop.aspectj.AnnotationAwareAspectJAutoProxyCreator;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.RootBeanDefinition;
import com.github.datalking.util.Assert;

/**
 * @author yaoo on 4/17/18
 */
public class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 创建代理对象的类相应bean的内部名称
     */
    public static final String AUTO_PROXY_CREATOR_BEAN_NAME = "play.aop.aspectj.internalAnnotationAwareAspectJAutoProxyCreator";

    /**
     * 注册 AnnotationAwareAspectJAutoProxyCreator  beanDefinition
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

        if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {

//            return registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
            return ;
        }

        Class cls = AnnotationAwareAspectJAutoProxyCreator.class;

        RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);

//        beanDefinition.getPropertyValues().addPropertyValue("order", -9999);
        registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);

//        return beanDefinition;
    }


}
