package com.github.datalking.aop.aspectj;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.common.Ordered;
import com.github.datalking.util.Assert;

import java.io.Serializable;

/**
 * @author yaoo on 4/19/18
 */
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {

    private final BeanFactory beanFactory;

    private final String name;

    private final AspectMetadata aspectMetadata;


    public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name, Class<?> type) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        Assert.notNull(name, "Bean name must not be null");
        this.beanFactory = beanFactory;
        this.name = name;
        this.aspectMetadata = new AspectMetadata(type, name);
    }


    public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
        this(beanFactory, name, beanFactory.getType(name));
    }

    @Override
    public Object getAspectInstance() {

        Object bean = null;
        try {
            bean = this.beanFactory.getBean(this.name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bean;
    }

//    @Override
//    public ClassLoader getAspectClassLoader() {
//        return (this.beanFactory instanceof ConfigurableBeanFactory ?
//                ((ConfigurableBeanFactory) this.beanFactory).getBeanClassLoader() :
//                ClassUtils.getDefaultClassLoader());
//    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return this.aspectMetadata;
    }


    @Override
    public int getOrder() {
        Class<?> type = this.beanFactory.getType(this.name);
        if (type != null) {
            if (Ordered.class.isAssignableFrom(type)) {

                Object bean = null;
                try {
                    bean = this.beanFactory.getBean(this.name);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return ((Ordered) bean).getOrder();
            }
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": bean name '" + this.name + "'";
    }


}
