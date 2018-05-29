package com.github.datalking.exception;

/**
 * @author yaoo on 5/29/18
 */
public class BeanDefinitionStoreException extends BeansException {

    private String resourceDescription;

    private String beanName;

    public BeanDefinitionStoreException(String msg) {
        super(msg);
    }

    public BeanDefinitionStoreException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BeanDefinitionStoreException(String resourceDescription, String msg) {
        super(msg);
        this.resourceDescription = resourceDescription;
    }

    /**
     * Create a new BeanDefinitionStoreException.
     *
     * @param resourceDescription description of the resource that the bean definition came from
     * @param msg                 the detail message (used as exception message as-is)
     * @param cause               the root cause (may be {@code null})
     */
    public BeanDefinitionStoreException(String resourceDescription, String msg, Throwable cause) {
        super(msg, cause);
        this.resourceDescription = resourceDescription;
    }

    /**
     * Create a new BeanDefinitionStoreException.
     *
     * @param resourceDescription description of the resource that the bean definition came from
     * @param beanName            the name of the bean requested
     * @param msg                 the detail message (appended to an introductory message that indicates
     *                            the resource and the name of the bean)
     */
    public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg) {
        this(resourceDescription, beanName, msg, null);
    }

    /**
     * Create a new BeanDefinitionStoreException.
     *
     * @param resourceDescription description of the resource that the bean definition came from
     * @param beanName            the name of the bean requested
     * @param msg                 the detail message (appended to an introductory message that indicates
     *                            the resource and the name of the bean)
     * @param cause               the root cause (may be {@code null})
     */
    public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg, Throwable cause) {
        super("Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
    }


    /**
     * Return the description of the resource that the bean
     * definition came from, if any.
     */
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    /**
     * Return the name of the bean requested, if any.
     */
    public String getBeanName() {
        return this.beanName;
    }

}
