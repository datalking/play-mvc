package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Advisor;
import com.github.datalking.beans.factory.ListableBeanFactory;
import org.aspectj.lang.reflect.PerClauseKind;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从BeanFactory查找所有@Aspect bean并创建advisor，便于自动创建代理
 *
 * @author yaoo on 4/18/18
 */
public class BeanFactoryAspectJAdvisorsBuilder {

    private final ListableBeanFactory beanFactory;

    private final AspectJAdvisorFactory advisorFactory;

    private volatile List<String> aspectBeanNames;

    private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

    private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();


    public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
        this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
    }

    public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
        this.beanFactory = beanFactory;
        this.advisorFactory = advisorFactory;
    }

    /**
     * 在beanFactory中寻找@Aspect注解标注的bean，并创建advisor
     */
    public List<Advisor> buildAspectJAdvisors() {

        List<String> aspectNames = this.aspectBeanNames;

        if (aspectNames == null) {
            {
                synchronized (this) {
                    aspectNames = this.aspectBeanNames;
                    if (aspectNames == null) {

                        List<Advisor> advisors = new LinkedList<>();
                        aspectNames = new LinkedList<>();

                        // 获取所有的beanNames
                        String[] beanNames = beanFactory.getBeanNamesForType(Object.class);

                        //循环所有beanName找出对应的增强方法
                        for (String beanName : beanNames) {

//                            if (beanName.equals("myAspect")) {
//                                System.out.println("==== BeanFactoryAspectJAdvisorsBuilder " + beanName);
//                            }

                            // 获取Bean的class列表
                            Class<?> beanType = this.beanFactory.getType(beanName);
                            if (beanType == null) {
                                continue;
                            }
                            // 判断是class上否包含@Aspect注解
                            if (this.advisorFactory.isAspect(beanType)) {
                                aspectNames.add(beanName);

                                AspectMetadata amd = new AspectMetadata(beanType, beanName);
                                if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                                    MetadataAwareAspectInstanceFactory factory = new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);

                                    //==== 解析标记AspectJ注解的增强方法
                                    List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);

                                    this.advisorsCache.put(beanName, classAdvisors);
//                                    if (this.beanFactory.isSingleton(beanName)) {
//                                    } else {
//                                        this.aspectFactoryCache.put(beanName, factory);
//                                    }

                                    advisors.addAll(classAdvisors);

                                } else {

                                    MetadataAwareAspectInstanceFactory factory = new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                                    this.aspectFactoryCache.put(beanName, factory);
                                    advisors.addAll(this.advisorFactory.getAdvisors(factory));
                                }
                            }
                        }
                        this.aspectBeanNames = aspectNames;
                        return advisors;
                    }
                }
            }
        }

        if (aspectNames.isEmpty()) {
            return Collections.emptyList();
        }

        //记录在缓存中
        List<Advisor> advisors = new LinkedList<>();

        for (String aspectName : aspectNames) {
            List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);

            if (cachedAdvisors != null) {
                advisors.addAll(cachedAdvisors);
            } else {
                MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
                advisors.addAll(this.advisorFactory.getAdvisors(factory));
            }

        }

        return advisors;

    }


}
