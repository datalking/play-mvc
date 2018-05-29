package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.DependencyDescriptor;

/**
 * @author yaoo on 5/29/18
 */
public interface AutowireCandidateResolver {

    boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);

    Object getSuggestedValue(DependencyDescriptor descriptor);

}
