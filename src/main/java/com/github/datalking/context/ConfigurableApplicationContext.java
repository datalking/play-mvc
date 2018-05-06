package com.github.datalking.context;

/**
 * 可配置的ApplicationContext 接口
 *
 * @author yaoo on 4/6/18
 */
public interface ConfigurableApplicationContext extends ApplicationContext {

    void refresh();

    boolean isActive();

    void setParent(ApplicationContext parent);

    void close();

}
