package com.github.datalking.context.annotation;

import com.github.datalking.annotation.Configuration;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.support.AbstractAutowireCapableBeanFactory;
import com.github.datalking.beans.factory.support.AbstractBeanDefinition;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import com.github.datalking.common.Ordered;
import com.github.datalking.common.PriorityOrdered;
import com.github.datalking.common.env.Environment;
import com.github.datalking.context.EnvironmentAware;
import com.github.datalking.util.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 执行扫描所有BeanDefinition中的@Configuration、@Bean、@ComponentScan
 * 实现了BeanFactoryPostProcessor接口
 *
 * @author yaoo on 4/13/18
 */
public class ConfigurationClassPostProcessor
        implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware {

    private ConfigurationClassBeanDefinitionReader reader;

    private Environment environment;

    // 处理registry中所有带有@Configuration的类的入口方法
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {

        // 扫描@Configuration、@Bean、@ComponentScan
        processConfigBeanDefinitions(registry);

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

        // 可以作增强处理
        // enhanceConfigurationClasses(beanFactory);

    }

    /**
     * 扫描BeanDefinitionRegistry中所有标注有@Configuration的类
     * 并解析该类的其他注解信息,如@Import
     */
    private void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {

        // 获取所有已注册的BeanDefinition
        String[] candidateNames = registry.getBeanDefinitionNames();

        // 用来保存所有带有@Configuration注解的对象，最初一般是显式指定的类
        Set<BeanDefinitionHolder> configCandidates = new LinkedHashSet<>();

        /// 遍历所有BeanDefinition，查找其中带有@Configuration的对象
        for (String beanName : candidateNames) {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            if (beanDef.getBeanClassName() == null) {
                return;
            }

            /// 加载类
            Class beanClass = ((AbstractAutowireCapableBeanFactory) registry).doResolveBeanClass((AbstractBeanDefinition) beanDef);
            ((AbstractBeanDefinition) beanDef).setBeanClass(beanClass);

            // 获取所有被@Configuration标注的类 对应的BeanDefinition
            // spring的实现所设置的范围包括所有full和lite
            if (beanClass != null && beanClass.isAnnotationPresent(Configuration.class)) {
                configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
            }
        }

        // 若没有注解@Configuration标注的类，则直接返回
        if (configCandidates.isEmpty()) {
            return;
        }

//        Collections.sort(configCandidates, new Comparator

        // todo configCandidates按照指定的@Order排序

        // 进一步解析@Configuration标注类的信息的解析器
        ConfigurationClassParser parser = new ConfigurationClassParser(registry, environment);

        // 记录带有@Configuration注解的candidates
        Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
        // 记录已解析的类
        Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());

        /// 若candidates非空，则循环执行，一般执行一次后就为空
        do {
            // 解析@Bean、@ComponentScan、@Import，将所有解析的信息保存到各自的configClasses
            parser.parse(configCandidates);

            // 获取已经解析过的类，一般是该@Configuration标注类上@Import指定的类
            Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
            configClasses.removeAll(alreadyParsed);

            if (this.reader == null) {
                reader = new ConfigurationClassBeanDefinitionReader(registry,this.environment);
            }

            // 将上面扫描到的bean和带有@Bean注解方法指定的bean注册到beanDefinitionMap，包括扫描mvc的BeanDefinition，但未实例化
            reader.loadBeanDefinitions(configClasses);

            alreadyParsed.addAll(configClasses);
            candidates.clear();

            /// 处理上面扫描期间新增的bean
            if (registry.getBeanDefinitionCount() > candidateNames.length) {

                String[] newCandidateNames = registry.getBeanDefinitionNames();
                Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
                Set<String> alreadyParsedClasses = new HashSet<>();

                for (ConfigurationClass configurationClass : alreadyParsed) {
                    alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
                }

                for (String candidateName : newCandidateNames) {

                    if (!oldCandidateNames.contains(candidateName)) {
                        BeanDefinition bd = registry.getBeanDefinition(candidateName);
                        // if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory)
                        /// 加载类
                        Class beanClass = ((AbstractAutowireCapableBeanFactory) registry).doResolveBeanClass((AbstractBeanDefinition) bd);
                        ((AbstractBeanDefinition) bd).setBeanClass(beanClass);

                        if (beanClass != null && beanClass.isAnnotationPresent(Configuration.class)
                                && !alreadyParsedClasses.contains(bd.getBeanClassName())) {
                            candidates.add(new BeanDefinitionHolder(bd, candidateName));
                        }
                    }
                }

                candidateNames = newCandidateNames;
            }

        } while (!candidates.isEmpty());


    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setEnvironment(Environment environment) {
        Assert.notNull(environment, "Environment must not be null");
        this.environment = environment;
    }

}
