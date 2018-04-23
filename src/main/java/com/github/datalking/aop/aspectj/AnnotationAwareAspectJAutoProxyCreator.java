package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Advisor;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于注解创建解析与创建代理
 *
 * @author yaoo on 4/17/18
 */
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

    private AspectJAdvisorFactory aspectJAdvisorFactory;
    private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;

    public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
        Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
        this.aspectJAdvisorFactory = aspectJAdvisorFactory;
    }


    @Override
    protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.initBeanFactory(beanFactory);
        if (this.aspectJAdvisorFactory == null) {
            this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
        }
        this.aspectJAdvisorsBuilder = new BeanFactoryAspectJAdvisorsBuilder(beanFactory, this.aspectJAdvisorFactory);
    }


    /**
     * 获取所有增强器
     */
    @Override
    protected List<Advisor> findCandidateAdvisors() {

        // 先调用父类的方法获取xml文件中配置的AOP advisor
        List<Advisor> advisors = super.findCandidateAdvisors();
        //List<Advisor> advisors = new ArrayList<>();

        // 获取有注解@Aspect的advisor
        advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());

        return advisors;
    }


}
