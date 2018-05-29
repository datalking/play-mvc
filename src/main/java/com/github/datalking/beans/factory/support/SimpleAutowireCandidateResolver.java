package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.DependencyDescriptor;

/**
 * 没有注解或不支持使用注解时使用，此实现只简单检查 BeanDefinition
 *
 * @author yaoo on 5/29/18
 */
public class SimpleAutowireCandidateResolver implements AutowireCandidateResolver {

    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return bdHolder.getBeanDefinition().isAutowireCandidate();
    }

    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        return null;
    }

}
