package com.github.datalking.context.annotation;

import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import java.beans.PropertyDescriptor;

/**
 * @author yaoo on 6/1/18
 */
public class ImportAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    private final BeanFactory beanFactory;

    public ImportAwareBeanPostProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {

//        if (bean instanceof EnhancedConfiguration) {
//            ((EnhancedConfiguration) bean).setBeanFactory(this.beanFactory);
//        }

        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
//        if (bean instanceof ImportAware) {
//            ImportRegistry ir = this.beanFactory.getBean(IMPORT_REGISTRY_BEAN_NAME, ImportRegistry.class);
//            AnnotationMetadata importingClass = ir.getImportingClassFor(bean.getClass().getSuperclass().getName());
//            if (importingClass != null) {
//                ((ImportAware) bean).setImportMetadata(importingClass);
//            }
//        }
        return bean;
    }
}
