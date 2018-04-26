package com.github.datalking.beans.factory;

/**
 * BeanFactory 根接口
 */
public interface BeanFactory {

    String FACTORY_BEAN_PREFIX = "&";

    // 可以用别名查找
    Object getBean(String name);

    Class<?> getType(String name);

    // 检查name对应的bean是否是targetType的类型
    boolean isTypeMatch(String name, Class<?> targetType);

//    boolean isPrototype(String name) ;

//    boolean isSingleton(String name) ;

    //类型可以是接口或者子类,但不能是null
//    <T> T getBean(Class<T> requiredType) throws Exception;
//    <T> T getBean(String name, Class<T> requiredType) throws Exception;

    //不管类是否抽象类,懒加载,是否在容器范围内,只要符合都返回true,所以这边true,不一定能从getBean获取实例
    boolean containsBean(String name);

//    String[] getAliases(String name);


}
