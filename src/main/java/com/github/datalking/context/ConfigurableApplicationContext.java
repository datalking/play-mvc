package com.github.datalking.context;

import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 可配置的ApplicationContext 接口
 *
 * @author yaoo on 4/6/18
 */
public interface ConfigurableApplicationContext extends ApplicationContext {

    String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

    String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

    String ENVIRONMENT_BEAN_NAME = "environment";

    void refresh();

    boolean isActive();

    void setParent(ApplicationContext parent);

    void close();

    ConfigurableListableBeanFactory getBeanFactory();

}
