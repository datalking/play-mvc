package com.github.datalking.context.annotation;

import com.github.datalking.common.meta.MethodMetadata;

/**
 * ConfigurationClass中带有@Bean注解的方法
 *
 * @author yaoo on 4/13/18
 */
public class BeanMethod {

    protected final MethodMetadata metadata;

    protected final ConfigurationClass configurationClass;

    public BeanMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
        this.metadata = metadata;
        this.configurationClass = configurationClass;
    }

    public MethodMetadata getMetadata() {
        return metadata;
    }

    public ConfigurationClass getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public String toString() {
        return "BMethod{" +
                "mName=" + metadata.getMethodName() +
                ", mReturn=" + metadata.getReturnTypeName() +
                ", mClass=" + metadata.getDeclaringClassName() +
                '}';
    }
}
