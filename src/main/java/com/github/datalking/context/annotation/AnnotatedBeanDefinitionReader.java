package com.github.datalking.context.annotation;

import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.support.AnnotatedGenericBeanDefinition;
import com.github.datalking.beans.factory.support.BeanDefinitionReaderUtils;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.RootBeanDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 基于注解的BeanDefinition读取器
 *
 * @author yaoo on 4/9/18
 */
public class AnnotatedBeanDefinitionReader {

    public final String CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME = "play.context.annotation.internalConfigurationClassPostProcessor";

    public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME = "play.context.annotation.internalAutowiredAnnotationBeanPostProcessor";

    public static final String REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME = "play.context.annotation.internalRequiredAnnotationProcessor";


    private final BeanDefinitionRegistry registry;

    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;

        //==== 注册常用的工具bean
        registerAnnotationConfigProcessors(this.registry);
    }


    public void register(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            registerBean(annotatedClass);
        }
    }

    public void registerBean(Class<?> annotatedClass) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);

//        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

        String beanName = BeanDefinitionReaderUtils.generateAnnotatedBeanName(abd, this.registry);

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);

    }

    /**
     * 注册内部使用的BeanDefinition
     */
    public Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {

        Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>();

        if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
            BeanDefinitionHolder holder = new BeanDefinitionHolder(def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME);
            beanDefs.add(holder);
            BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
        }

        if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
            BeanDefinitionHolder holder = new BeanDefinitionHolder(def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME);
//            def.setSource(source);
            BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
        }

//        if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
//            RootBeanDefinition def = new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class);
//            def.setSource(source);
//            beanDefs.add(registerPostProcessor(registry, def, REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
//        }

        return beanDefs;
    }


    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }


}
