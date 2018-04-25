package com.github.datalking.context;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.ListableBeanFactory;

/**
 * 应用运行环境ApplicationContext 根接口
 */
public interface ApplicationContext extends ListableBeanFactory {

    String getId();

//    String getApplicationName();

//    String getDisplayName();

//    long getStartupDate();

    ApplicationContext getParent();

//    AutowireCapableBeanFactory getAutowireCapableBeanFactory();

}
