package com.github.datalking.context.support;

import com.github.datalking.aop.aspectj.AnnotationAwareAspectJAutoProxyCreator;
import com.github.datalking.beans.factory.config.BeanFactoryPostProcessor;
import com.github.datalking.beans.factory.config.BeanPostProcessor;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import com.github.datalking.common.Ordered;
import com.github.datalking.common.PriorityOrdered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BeanFactoryPostProcessor相关功能执行的代理类
 * 一是触发beanFactoryPostProcessor，二是注册beanFactoryPostProcessor
 *
 * @author yaoo on 4/13/18
 */
public class PostProcessorRegistrationDelegate {

    public static void invokeBeanFactoryPostProcessors(
            Collection<? extends BeanFactoryPostProcessor> postProcessors,
            ConfigurableListableBeanFactory beanFactory) {

        for (BeanFactoryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    public static void invokeBeanFactoryPostProcessors(
            ConfigurableListableBeanFactory beanFactory,
            List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

        Set<String> processedBeans = new HashSet<>();

        /// 如果beanFactory是可注册BeanDefinition的，则处理实现了BeanDefinitionRegistryPostProcessor接口的bean
        if (beanFactory instanceof BeanDefinitionRegistry) {

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

            // 遍历BeanDefinitionMap和singletonObjects，获取BeanDefinitionRegistryPostProcessor类型的bean名称
            String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class);

            // 实例化默认的后置处理器，包括ConfigurationClassPostProcessor
            List<BeanDefinitionRegistryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
            for (String ppName : postProcessorNames) {
                priorityOrderedPostProcessors.add((BeanDefinitionRegistryPostProcessor) beanFactory.getBean(ppName));
                processedBeans.add(ppName);
            }
//            sortPostProcessors(beanFactory, priorityOrderedPostProcessors);

            // 扫描BeanDefinitionMap中带有@Configuration的类，再进一步扫描@Bean、@ComponentScan
            // 注册AnnotationAwareAspectJAutoProxyCreator的BeanDefinition
            invokeBeanDefinitionRegistryPostProcessors(priorityOrderedPostProcessors, registry);
        }
        /// 如果beanFactory不可注册BeanDefinition
        else {
            invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
        }

        /// 处理上面过程中新增的BeanFactoryPostProcessor
        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class);
        List<BeanFactoryPostProcessor> postProcessors = new ArrayList<>();

        for (String postProcessorName : postProcessorNames) {
            /// 若已经处理过，则跳过
            if (!processedBeans.contains(postProcessorName)) {
                postProcessors.add((BeanFactoryPostProcessor) beanFactory.getBean(postProcessorName));
            }
        }

        invokeBeanFactoryPostProcessors(postProcessors, beanFactory);
    }


    private static void invokeBeanDefinitionRegistryPostProcessors(
            Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors,
            BeanDefinitionRegistry registry) {

        for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanDefinitionRegistry(registry);
        }
    }


    public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
                                                  AbstractApplicationContext applicationContext) {

        // 获取BeanPostProcessor类型的bean名
        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class);

        List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
        List<String> orderedPostProcessorNames = new ArrayList<>();
        List<String> nonOrderedPostProcessorNames = new ArrayList<>();


        for (String ppName : postProcessorNames) {

            if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                //BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                BeanPostProcessor pp = (BeanPostProcessor) beanFactory.getBean(ppName);
                priorityOrderedPostProcessors.add(pp);
            } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                orderedPostProcessorNames.add(ppName);
            } else {
                nonOrderedPostProcessorNames.add(ppName);
            }
        }
        registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

        // 下面实例化 AutoProxyCreator
        List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
        for (String ppName : orderedPostProcessorNames) {
            BeanPostProcessor pp = (BeanPostProcessor) beanFactory.getBean(ppName);
            orderedPostProcessors.add(pp);
            // 特殊初始化AnnotationAwareAspectJAutoProxyCreator的bean
            if (pp instanceof AnnotationAwareAspectJAutoProxyCreator) {
                ((AnnotationAwareAspectJAutoProxyCreator) pp).setBeanFactory(beanFactory);
            }
        }

        registerBeanPostProcessors(beanFactory, orderedPostProcessors);

        List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
        for (String ppName : nonOrderedPostProcessorNames) {
            BeanPostProcessor pp = (BeanPostProcessor) beanFactory.getBean(ppName);
            nonOrderedPostProcessors.add(pp);
        }
        registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

    }

    private static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
                                                   List<BeanPostProcessor> postProcessors) {

        for (BeanPostProcessor postProcessor : postProcessors) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
    }


}
