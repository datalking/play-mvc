package com.github.datalking.context.annotation;

import com.github.datalking.annotation.meta.AnnotationMetadata;
import com.github.datalking.annotation.meta.MethodMetadata;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.support.AnnotatedGenericBeanDefinition;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.ConfigurationClassBeanDefinition;

import java.util.Map;
import java.util.Set;

/**
 * 将ConfigurationClass的类信息注册到beanDefinitionMap
 *
 * @author yaoo on 4/13/18
 */
public class ConfigurationClassBeanDefinitionReader {

    private final BeanDefinitionRegistry registry;

    public ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * 将ConfigurationClass的类信息注册到beanDefinitionMap
     */
    public void loadBeanDefinitions(Set<ConfigurationClass> configurationModel) {

        for (ConfigurationClass configClass : configurationModel) {

            loadBeanDefinitionsForConfigurationClass(configClass);
        }

    }


    private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configClass) {

        if (configClass.isImported()) {
            registerBeanDefinitionForImportedConfigurationClass(configClass);
        }

        for (BeanMethod beanMethod : configClass.getBeanMethods()) {

            // 查找带有@Bean注解的方法，并注册BeanDefinition
            loadBeanDefinitionsForBeanMethod(beanMethod);
        }

        // 查找@Import注解上的bean定义，并注册BeanDefinition
        loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());

    }

    private void registerBeanDefinitionForImportedConfigurationClass(ConfigurationClass configClass) {
        AnnotationMetadata metadata = configClass.getMetadata();
        AnnotatedGenericBeanDefinition configBeanDef = new AnnotatedGenericBeanDefinition(metadata);

//        ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(configBeanDef);
//        configBeanDef.setScope(scopeMetadata.getScopeName());
        String configBeanName = configClass.getBeanName();
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(configBeanDef, configBeanName);
        this.registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
//        configClass.setBeanName(configBeanName);

    }

    private void loadBeanDefinitionsForBeanMethod(BeanMethod beanMethod) {

        ConfigurationClass configClass = beanMethod.getConfigurationClass();

        // 获取方法元信息
        MethodMetadata metadata = beanMethod.getMetadata();
        String methodName = metadata.getMethodName();
        // todo beanName默认为方法名，要支持获取自定义bean名
        String beanName = methodName;

        ConfigurationClassBeanDefinition beanDef = new ConfigurationClassBeanDefinition(configClass, metadata);

        // 设置FactoryBeanName和FactoryMethodName
        beanDef.setFactoryBeanName(configClass.getBeanName());
        beanDef.setFactoryMethodName(methodName);

        this.registry.registerBeanDefinition(beanName, beanDef);

    }


    private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {

        for (Map.Entry<ImportBeanDefinitionRegistrar, AnnotationMetadata> entry : registrars.entrySet()) {
            entry.getKey().registerBeanDefinitions(entry.getValue(), this.registry);
        }

    }

}
