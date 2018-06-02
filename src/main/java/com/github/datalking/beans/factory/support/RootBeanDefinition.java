package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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

    Object resolvedConstructorOrFactoryMethod;

    boolean constructorArgumentsResolved = false;

    Object[] resolvedConstructorArguments;

    Object[] preparedConstructorArguments;

    public boolean isFactoryMethodUnique = false;

    public boolean allowCaching = true;

    boolean postProcessed = false;

    private Set<Member> externallyManagedConfigMembers;

    // volatile ResolvableType factoryMethodReturnType;

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
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;


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

    public Method getResolvedFactoryMethod() {

//        synchronized (this.constructorArgumentLock) {
        Object candidate = this.resolvedConstructorOrFactoryMethod;
        return (candidate instanceof Method ? (Method) candidate : null);
//        }
    }

    public void registerExternallyManagedConfigMember(Member configMember) {
//        synchronized (this.postProcessingLock) {
            if (this.externallyManagedConfigMembers == null) {
                this.externallyManagedConfigMembers = new HashSet<Member>(1);
            }
            this.externallyManagedConfigMembers.add(configMember);
//        }
    }

    public boolean isExternallyManagedConfigMember(Member configMember) {
//        synchronized (this.postProcessingLock) {
            return (this.externallyManagedConfigMembers != null &&
                    this.externallyManagedConfigMembers.contains(configMember));
//        }
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
