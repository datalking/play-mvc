package com.github.datalking.context.annotation;

import com.github.datalking.beans.factory.config.AnnotatedBeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.DefaultListableBeanFactory;
import com.github.datalking.beans.factory.support.QualifierAnnotationAutowireCandidateResolver;
import com.github.datalking.beans.factory.support.RootBeanDefinition;
import com.github.datalking.common.meta.AnnotationAttributes;
import com.github.datalking.common.meta.AnnotationMetadata;
import com.github.datalking.util.ClassUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 注册常用BeanPostProcessor和BeanFactoryPostProcessor的工具类
 *
 * @author yaoo on 5/28/18
 */
public class AnnotationConfigUtils {

    public static final String CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalConfigurationAnnotationProcessor";

    public static final String CONFIGURATION_BEAN_NAME_GENERATOR =
            "org.springframework.context.annotation.internalConfigurationBeanNameGenerator";

    public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalAutowiredAnnotationProcessor";

    public static final String REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalRequiredAnnotationProcessor";

    public static final String COMMON_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalCommonAnnotationProcessor";

    public static final String PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalPersistenceAnnotationProcessor";

    private static final String PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME =
            "org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor";

    public static final String SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalScheduledAnnotationProcessor";

    public static final String ASYNC_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalAsyncAnnotationProcessor";

    public static final String ASYNC_EXECUTION_ASPECT_BEAN_NAME =
            "org.springframework.scheduling.config.internalAsyncExecutionAspect";

    public static final String ASYNC_EXECUTION_ASPECT_CLASS_NAME =
            "org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect";

    public static final String ASYNC_EXECUTION_ASPECT_CONFIGURATION_CLASS_NAME =
            "org.springframework.scheduling.aspectj.AspectJAsyncConfiguration";

    public static final String CACHE_ADVISOR_BEAN_NAME =
            "org.springframework.cache.config.internalCacheAdvisor";

    public static final String CACHE_ASPECT_BEAN_NAME =
            "org.springframework.cache.config.internalCacheAspect";

    public static final String CACHE_ASPECT_CLASS_NAME =
            "org.springframework.cache.aspectj.AnnotationCacheAspect";

    public static final String CACHE_ASPECT_CONFIGURATION_CLASS_NAME =
            "org.springframework.cache.aspectj.AspectJCachingConfiguration";


    private static final boolean jsr250Present =
            ClassUtils.isPresent("javax.annotation.Resource", AnnotationConfigUtils.class.getClassLoader());

    private static final boolean jpaPresent =
            ClassUtils.isPresent("javax.persistence.EntityManagerFactory", AnnotationConfigUtils.class.getClassLoader()) &&
                    ClassUtils.isPresent(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, AnnotationConfigUtils.class.getClassLoader());


    public static void registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {
        registerAnnotationConfigProcessors(registry, null);
    }

    public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
            BeanDefinitionRegistry registry, Object source) {

        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        if (!(beanFactory.getAutowireCandidateResolver() instanceof QualifierAnnotationAutowireCandidateResolver)) {
            beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
        }

        Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(4);

        if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
//            def.setSource(source);
            beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
        }

        if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
//            def.setSource(source);
            beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
        }

//        if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
//            RootBeanDefinition def = new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class);
////            def.setSource(source);
//            beanDefs.add(registerPostProcessor(registry, def, REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
//        }

        // Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
//        if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
//            RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
////            def.setSource(source);
//            beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
//        }

        // Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
//        if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
//            RootBeanDefinition def = new RootBeanDefinition();
//            try {
//                def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
//                        AnnotationConfigUtils.class.getClassLoader()));
//            } catch (ClassNotFoundException ex) {
//                throw new IllegalStateException(
//                        "Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
//            }
////            def.setSource(source);
//            beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
//        }

        return beanDefs;
    }

    private static BeanDefinitionHolder registerPostProcessor(BeanDefinitionRegistry registry,
                                                              RootBeanDefinition definition,
                                                              String beanName) {

        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(beanName, definition);
        return new BeanDefinitionHolder(definition, beanName);
    }

    public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd) {
//        AnnotationMetadata metadata = abd.getMetadata();
//        if (metadata.isAnnotated(Primary.class.getName())) {
//            abd.setPrimary(true);
//        }
//        if (metadata.isAnnotated(Lazy.class.getName())) {
//            abd.setLazyInit(MetadataUtils.attributesFor(metadata, Lazy.class).getBoolean("value"));
//        }
//        if (metadata.isAnnotated(DependsOn.class.getName())) {
//            abd.setDependsOn(MetadataUtils.attributesFor(metadata, DependsOn.class).getStringArray("value"));
//        }
//        if (abd instanceof AbstractBeanDefinition) {
//            if (metadata.isAnnotated(Role.class.getName())) {
//                Integer role = MetadataUtils.attributesFor(metadata, Role.class).getNumber("value");
//                ((AbstractBeanDefinition) abd).setRole(role);
//            }
//        }
    }

//    static BeanDefinitionHolder applyScopedProxyMode(ScopeMetadata metadata, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
//
//        ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
//        if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
//            return definition;
//        }
//        boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
//        return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
//    }

    static Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
                                                             Class<?> containerClass,
                                                             Class<?> annotationClass) {

        if (containerClass == null) {
            return attributesForRepeatable(metadata, null, annotationClass.getName());

        } else {

            return attributesForRepeatable(metadata, containerClass.getName(), annotationClass.getName());
        }
    }

    static Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
                                                             String containerClassName,
                                                             String annotationClassName) {

        Set<AnnotationAttributes> result = new LinkedHashSet<>();

        addAttributesIfNotNull(result, metadata.getAnnotationAttributes(annotationClassName, false));

        if (containerClassName != null) {
            Map<String, Object> container = metadata.getAnnotationAttributes(containerClassName, false);

            if (container != null && container.containsKey("value")) {
                for (Map<String, Object> containedAttributes : (Map<String, Object>[]) container.get("value")) {

                    addAttributesIfNotNull(result, containedAttributes);
                }
            }
        }

        return Collections.unmodifiableSet(result);
    }

    private static void addAttributesIfNotNull(Set<AnnotationAttributes> result, Map<String, Object> attributes) {
        if (attributes != null) {
            result.add(AnnotationAttributes.fromMap(attributes));
        }
    }

}
