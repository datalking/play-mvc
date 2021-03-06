package com.github.datalking.beans.factory.support;

import com.github.datalking.common.meta.AnnotationMetadata;
import com.github.datalking.common.meta.StandardAnnotationMetadata;
import com.github.datalking.beans.factory.config.AnnotatedBeanDefinition;
import com.github.datalking.util.Assert;

/**
 * 存储注解元信息的 BeanDefinition
 *
 * @author yaoo on 4/9/18
 */
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

    private final AnnotationMetadata metadata;

    public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
        setBeanClassName(beanClass.getName());
        this.metadata = new StandardAnnotationMetadata(beanClass);
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
        Assert.notNull(metadata, "AnnotationMetadata must not be null");
        if (metadata instanceof StandardAnnotationMetadata) {
            setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
        }
        else {
            setBeanClassName(metadata.getClassName());
        }
        this.metadata = metadata;
    }

    @Override
    public AnnotationMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "AnnoGenericBDef: " + super.toString();
    }


}
