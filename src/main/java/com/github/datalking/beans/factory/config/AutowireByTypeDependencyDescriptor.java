package com.github.datalking.beans.factory.config;

import com.github.datalking.common.MethodParameter;

/**
 * @author yaoo on 6/1/18
 */
public class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

    public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
        super(methodParameter, false, eager);
    }

    @Override
    public String getDependencyName() {
        return null;
    }

}
