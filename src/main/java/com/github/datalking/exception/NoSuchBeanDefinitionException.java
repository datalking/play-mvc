package com.github.datalking.exception;

import com.github.datalking.util.StringUtils;

/**
 * BeanDefinition未定义的异常
 *
 * @author yaoo on 4/4/18
 */
public class NoSuchBeanDefinitionException extends BeansException {

    /**
     * Name of the missing bean
     */
    private String beanName;

    /**
     * Required type of the missing bean
     */
    private Class<?> beanType;

    public NoSuchBeanDefinitionException(String name) {
        super("No bean named '" + name + "' is defined");
        this.beanName = name;
    }

    public NoSuchBeanDefinitionException(String name, String message) {
        super("No bean named '" + name + "' is defined: " + message);
        this.beanName = name;
    }

    public NoSuchBeanDefinitionException(Class<?> type) {
        super("No qualifying bean of type [" + type.getName() + "] is defined");
        this.beanType = type;
    }

    public NoSuchBeanDefinitionException(Class<?> type, String message) {
        super("No qualifying bean of type [" + type.getName() + "] is defined: " + message);
        this.beanType = type;
    }

    public NoSuchBeanDefinitionException(Class<?> type, String dependencyDescription, String message) {
        super("No qualifying bean of type [" + type.getName() + "] found for dependency" + (StringUtils.hasLength(dependencyDescription) ? " [" + dependencyDescription + "]" : "") +
                ": " + message);
        this.beanType = type;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public Class<?> getBeanType() {
        return this.beanType;
    }

    /**
     * Return the number of beans found when only one matching bean was expected.
     * For a regular NoSuchBeanDefinitionException, this will always be 0.
     */
    public int getNumberOfBeansFound() {
        return 0;
    }

}
