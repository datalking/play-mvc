package com.github.datalking.context.annotation;

import com.github.datalking.common.meta.AnnotationMetadata;
import com.github.datalking.common.meta.MethodMetadata;
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
     * 注册BeanDefinition的入口
     */
    public void loadBeanDefinitions(Set<ConfigurationClass> configurationModel) {

        /// 遍历ConfigurationClass并注册
        for (ConfigurationClass configClass : configurationModel) {

            loadBeanDefinitionsForConfigurationClass(configClass);
        }
    }


    /**
     * 将ConfigurationClass的自身和其中的beanMethod注册到beanDefinitionMap
     */
    private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configClass) {

        /// 只需处理注解或间隔@通过Import指定的类
        if (configClass.isImported()) {
            registerBeanDefinitionForImportedConfigurationClass(configClass);
        }

        for (BeanMethod beanMethod : configClass.getBeanMethods()) {

            // 查找带有@Bean注解的方法，并注册BeanDefinition
            loadBeanDefinitionsForBeanMethod(beanMethod);
        }

        // 获取@Import注解上指定的类的BeanDefinition，该类必须实现ImportBeanDefinitionRegistrar接口
        // 再调用接口中的方法注册需要的bean，如这里可以注册mybatis的dao
        loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
    }

    /**
     * 注册ConfigurationClass自身
     */
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

    /**
     * 注册ConfigurationClass中的BeanMethod
     */
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

    /**
     * 通过调用实现了ImportBeanDefinitionRegistrar接口的class的方法，注册需要的bean
     * 如注册mybatis的dao
     */
    private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {

        for (Map.Entry<ImportBeanDefinitionRegistrar, AnnotationMetadata> entry : registrars.entrySet()) {
            // 先获取ImportBeanDefinitionRegistrar对象，再调用该对象的方法注册BeanDefinition，本方法并不直接注册BeanDefinition
            entry.getKey().registerBeanDefinitions(entry.getValue(), this.registry);
        }
    }

}
