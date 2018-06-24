package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.RuntimeBeanReference;

/**
 * bean属性解析及引用类型转换
 *
 * @author yaoo on 4/8/18
 */
public class BeanDefinitionValueResolver {

    private AbstractBeanFactory beanFactory;

    private String beanName;

    private BeanDefinition beanDefinition;

    private TypeConverter typeConverter;

    public BeanDefinitionValueResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanDefinitionValueResolver(AbstractBeanFactory beanFactory,
                                       String beanName,
                                       BeanDefinition beanDefinition,
                                       TypeConverter typeConverter) {

        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.beanDefinition = beanDefinition;
        this.typeConverter = typeConverter;
    }

    public Object resolveValueIfNecessary(Object argName, Object value) {

        if (value instanceof RuntimeBeanReference) {

            RuntimeBeanReference ref = (RuntimeBeanReference) value;
            return resolveReference(argName, ref);

        }
        // ==== 处理字符串类型
        else if (value instanceof String) {
            return value;
        }

        return value;
    }

    private Object resolveReference(Object argName, RuntimeBeanReference ref) {

        String refName = ref.getBeanName();

        Object bean = this.beanFactory.getBean(refName);
//        this.beanFactory.registerDependentBean(refName, this.beanName);
        return bean;
    }


}
