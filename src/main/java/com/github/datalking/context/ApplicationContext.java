package com.github.datalking.context;

import com.github.datalking.beans.factory.ListableBeanFactory;
import com.github.datalking.beans.factory.config.AutowireCapableBeanFactory;
import com.github.datalking.io.ResourcePatternResolver;

/**
 * 应用运行环境ApplicationContext 根接口
 */
public interface ApplicationContext
        extends ListableBeanFactory, MessageSource, ResourcePatternResolver, ApplicationEventPublisher {

    String getId();

//    String getApplicationName();

//    String getDisplayName();

//    long getStartupDate();

    ApplicationContext getParent();

    AutowireCapableBeanFactory getAutowireCapableBeanFactory();

}
