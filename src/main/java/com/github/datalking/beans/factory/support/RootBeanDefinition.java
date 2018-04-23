package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;

import java.lang.reflect.Method;

/**
 * BeanDefinition高级实现类
 *
 * @author yaoo on 4/3/18
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

    private volatile Class<?> targetType;

    private BeanDefinitionHolder decoratedDefinition;

    // 记录是否已经执行post processor
    volatile Boolean beforeInstantiationResolved;

    // volatile ResolvableType factoryMethodReturnType;
    // Object[] resolvedConstructorArguments;


    public RootBeanDefinition() {
        super();
    }

    public RootBeanDefinition(Class<?> beanClass) {
        super();
        setBeanClass(beanClass);
    }

    public RootBeanDefinition(String beanClassName) {
        setBeanClassName(beanClassName);
    }

    public RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    public RootBeanDefinition(RootBeanDefinition original) {
        super(original);
        this.targetType = original.targetType;
        this.decoratedDefinition = original.decoratedDefinition;

    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public void setTargetType(Class<?> targetType) {
        this.targetType = targetType;
    }

    public BeanDefinitionHolder getDecoratedDefinition() {
        return this.decoratedDefinition;
    }

    public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
        this.decoratedDefinition = decoratedDefinition;
    }

    public boolean isFactoryMethod(Method candidate) {
        return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
    }

    @Override
    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }

    @Override
    public boolean equals(Object o) {
        return (this == o || (o instanceof RootBeanDefinition && super.equals(o)));
    }

    @Override
    public String toString() {
        return "RootBDef: " + super.toString();
    }


}
