package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.MutablePropertyValues;

/**
 * bean属性元数据定义 根接口
 *
 * @author yaoo on 4/3/18
 */
public interface BeanDefinition {

    /// bean的用途分类，3类
    // 应用程序重要的bean，多是用户定义的bean
    int ROLE_APPLICATION = 0;
    // 辅助类的bean，如配置类
    int ROLE_SUPPORT = 1;
    // 基础类的bean，多内部使用
    int ROLE_INFRASTRUCTURE = 2;

    String getBeanClassName();

    void setBeanClassName(String beanClassName);

    MutablePropertyValues getPropertyValues();

    boolean isLazyInit();

    void setLazyInit(boolean lazyInit);

    boolean isSingleton();

    String getFactoryBeanName();

    void setFactoryBeanName(String factoryBeanName);

    String getFactoryMethodName();

    void setFactoryMethodName(String factoryMethodName);

    int getRole();

    ConstructorArgumentValues getConstructorArgumentValues();

    boolean isAutowireCandidate();

    Class<?> getBeanClass();

//    void setDependsOn(String... dependsOn);
//
//    String[] getDependsOn();

//    boolean isPrimary();
//    boolean isPrototype();
//    boolean isAutowireCandidate();
//    // bean是否是抽象类，若是，则不会创建实例
//    boolean isAbstract();


}
