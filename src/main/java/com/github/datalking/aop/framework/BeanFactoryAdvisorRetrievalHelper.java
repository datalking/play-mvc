package com.github.datalking.aop.framework;

import com.github.datalking.aop.Advisor;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * 从beanFactory中查找advisor
 *
 * @author yaoo on 4/18/18
 */
public class BeanFactoryAdvisorRetrievalHelper {

    private final ConfigurableListableBeanFactory beanFactory;

    private String[] cachedAdvisorBeanNames;

    public BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 从当前beanFactory中查找所有advisor bean
     * 不要处理工厂bean
     */
    public List<Advisor> findAdvisorBeans() {

        String[] advisorNames;
        synchronized (this) {
            advisorNames = this.cachedAdvisorBeanNames;
            if (advisorNames == null) {
                // 获取实现了Advisor接口的Bean
                advisorNames = beanFactory.getBeanNamesForType(Advisor.class);
                this.cachedAdvisorBeanNames = advisorNames;
            }
        }

        if (advisorNames.length == 0) {
            return new LinkedList<>();
        }
        List<Advisor> advisors = new LinkedList<>();
        for (String name : advisorNames) {
            if (this.beanFactory.isCurrentlyInCreation(name)) {
                // 跳过正在创建的bean
                continue;
            } else {

                Advisor advisor = (Advisor) this.beanFactory.getBean(name);
                advisors.add(advisor);
            }
        }

        return advisors;


    }


}
