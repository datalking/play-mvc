package com.github.datalking.context.support;

import com.github.datalking.beans.factory.Aware;
import com.github.datalking.beans.factory.config.BeanPostProcessor;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.beans.factory.config.EmbeddedValueResolver;
import com.github.datalking.common.StringValueResolver;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.context.ApplicationEventPublisherAware;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.context.EmbeddedValueResolverAware;
import com.github.datalking.context.EnvironmentAware;
import com.github.datalking.context.MessageSource;
import com.github.datalking.context.MessageSourceAware;
import com.github.datalking.context.ResourceLoaderAware;

/**
 * @author yaoo on 5/6/18
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

    private final ConfigurableApplicationContext applicationContext;

    private final StringValueResolver embeddedValueResolver;

    public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) applicationContext.getAutowireCapableBeanFactory());
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, String beanName)  {

        invokeAwareInterfaces(bean);

        return bean;
    }

    private void invokeAwareInterfaces(Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof EnvironmentAware) {
                ((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
            }
            if (bean instanceof EmbeddedValueResolverAware) {
                ((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(this.embeddedValueResolver);
            }
            if (bean instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
            }
            if (bean instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
            }
            if (bean instanceof MessageSourceAware) {
                ((MessageSourceAware) bean).setMessageSource((MessageSource) this.applicationContext);
            }
            if (bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
            }
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
