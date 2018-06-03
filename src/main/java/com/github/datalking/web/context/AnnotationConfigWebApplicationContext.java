package com.github.datalking.web.context;

import com.github.datalking.beans.factory.support.DefaultListableBeanFactory;
import com.github.datalking.context.annotation.AnnotatedBeanDefinitionReader;
import com.github.datalking.context.annotation.ClassPathBeanDefinitionScanner;
import com.github.datalking.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author yaoo on 4/25/18
 */
public class AnnotationConfigWebApplicationContext extends AbstractWebApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(AnnotationConfigWebApplicationContext.class);

    private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();

    private final Set<String> basePackages = new LinkedHashSet<>();

    public void register(Class<?>... annotatedClasses) {
        Assert.notNull(annotatedClasses, "At least one annotated class must be specified");
        this.annotatedClasses.addAll(Arrays.asList(annotatedClasses));
    }

    public void scan(String... basePackages) {
        Assert.notNull(basePackages, "At least one base package must be specified");
        this.basePackages.addAll(Arrays.asList(basePackages));
    }


    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {

        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(beanFactory);
        // reader.setEnvironment(getEnvironment());

        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);
        // scanner.setEnvironment(getEnvironment());

        //BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
        //ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();
//        if (beanNameGenerator != null) {
//            reader.setBeanNameGenerator(beanNameGenerator);
//            scanner.setBeanNameGenerator(beanNameGenerator);
//            beanFactory.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
//        }
//        if (scopeMetadataResolver != null) {
//            reader.setScopeMetadataResolver(scopeMetadataResolver);
//            scanner.setScopeMetadataResolver(scopeMetadataResolver);
//        }

        /// 注册显式声明的class的BeanDefinition
        if (!this.annotatedClasses.isEmpty()) {

            reader.register(this.annotatedClasses.toArray(new Class<?>[this.annotatedClasses.size()]));
        }

        // 扫描指定包下的Bean
        if (!this.basePackages.isEmpty()) {

            scanner.scan(this.basePackages.toArray(new String[this.basePackages.size()]));
        }

        // 处理xml
//        String[] configLocations = getConfigLocations();
//        if (configLocations != null) {
//            for (String configLocation : configLocations) {
//                try {
//                    Class<?> clazz = getClassLoader().loadClass(configLocation);
//                    if (logger.isInfoEnabled()) {
//                        logger.info("Successfully resolved class for [" + configLocation + "]");
//                    }
//                    reader.register(clazz);
//                } catch (ClassNotFoundException ex) {
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("Could not load class for config location [" + configLocation +
//                                "] - trying package scan. " + ex);
//                    }
//                    int count = scanner.scan(configLocation);
//                    if (logger.isInfoEnabled()) {
//                        if (count == 0) {
//                            logger.info("No annotated classes found for specified class/package [" + configLocation + "]");
//                        } else {
//                            logger.info("Found " + count + " annotated classes in package [" + configLocation + "]");
//                        }
//                    }
//                }
//            }
//        }


    }


}
