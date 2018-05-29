package com.github.datalking.beans.factory.config;

import com.github.datalking.util.ObjectUtils;

/**
 * BeanDefinition的包装类
 * 存放别名
 * 仅库内部使用
 *
 * @author yaoo on 4/4/18
 */
public class BeanDefinitionHolder {

    private final BeanDefinition beanDefinition;
    private final String beanName;
    private final String[] aliases;

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
        this(beanDefinition, beanName, null);
    }

    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }

    public String[] getAliases() {
        return aliases;
    }

    public boolean matchesName(String candidateName) {
        return (candidateName != null
                && (candidateName.equals(this.beanName) || ObjectUtils.containsElement(this.aliases, candidateName)));
    }


}
