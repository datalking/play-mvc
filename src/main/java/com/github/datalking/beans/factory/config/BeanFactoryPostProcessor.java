package com.github.datalking.beans.factory.config;

/**
 * 容器扩展接口
 * <p>
 * 在容器注册了BeanDefinition之后，实例化之前执行
 * 通过这个接口可以获取Bean定义的元数据并且修改它们，如Bean的scope属性、property值等，也可以操作beanFactory
 *
 * @author yaoo on 4/13/18
 */
public interface BeanFactoryPostProcessor {

    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory);

}
