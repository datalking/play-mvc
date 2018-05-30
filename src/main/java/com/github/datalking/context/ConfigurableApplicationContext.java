package com.github.datalking.context;

import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.common.env.ConfigurableEnvironment;

/**
 * 可配置的ApplicationContext 接口
 *
 * @author yaoo on 4/6/18
 */
public interface ConfigurableApplicationContext extends ApplicationContext {

    String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

    String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

    String ENVIRONMENT_BEAN_NAME = "environment";

    String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";

    String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";

    void refresh();

    boolean isActive();

    void setParent(ApplicationContext parent);

    void close();

    ConfigurableListableBeanFactory getBeanFactory();

    ConfigurableEnvironment getEnvironment();

    void setEnvironment(ConfigurableEnvironment environment);

}
