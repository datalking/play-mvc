package com.github.datalking.context.annotation;

import com.github.datalking.common.meta.AnnotationMetadata;
import com.github.datalking.common.meta.StandardAnnotationMetadata;
import com.github.datalking.util.Assert;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 被@Configuration注解标记的类
 *
 * @author yaoo on 4/13/18
 */
public class ConfigurationClass {

    private String beanName;

    private final AnnotationMetadata metadata;

    /**
     * 该类中包含@Bean注解的方法
     */
    private final Set<BeanMethod> beanMethods = new LinkedHashSet<>();

    /**
     * 该类上@Import导入的类 -> 该类注解元数据，且作为key的类实现了ImportBeanDefinitionRegistrar接口
     */
    private final Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> importBeanDefinitionRegistrars = new LinkedHashMap<>();

    private boolean imported = false;

//    private final Set<ConfigurationClass> importedBy = new LinkedHashSet<>(1);
//    final Set<String> skippedBeanMethods = new HashSet<String>();

    public ConfigurationClass(AnnotationMetadata metadata, String beanName) {
        this.metadata = metadata;
        this.beanName = beanName;
    }

    public ConfigurationClass(Class<?> clazz, String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        this.metadata = new StandardAnnotationMetadata(clazz);
        this.beanName = beanName;
    }

    public ConfigurationClass(Class<?> clazz, ConfigurationClass importedBy) {
        this.metadata = new StandardAnnotationMetadata(clazz);
//        this.importedBy.add(importedBy);
    }

    public AnnotationMetadata getMetadata() {
        return this.metadata;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public void addBeanMethod(BeanMethod method) {
        this.beanMethods.add(method);
    }

    public Set<BeanMethod> getBeanMethods() {
        return this.beanMethods;
    }

    public void addImportBeanDefinitionRegistrar(ImportBeanDefinitionRegistrar registrar, AnnotationMetadata importingClassMetadata) {
        this.importBeanDefinitionRegistrars.put(registrar, importingClassMetadata);
    }

    public Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> getImportBeanDefinitionRegistrars() {
        return this.importBeanDefinitionRegistrars;
    }

    public boolean isImported() {
//        return !this.importedBy.isEmpty();
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

//    public Set<ConfigurationClass> getImportedBy() {
//        return this.importedBy;
//    }
//
//    public boolean addImportedBy(ConfigurationClass c) {
//        return this.importedBy.add(c);
//    }

    @Override
    public boolean equals(Object other) {
        return (this == other ||
                (other instanceof ConfigurationClass && getMetadata().getClassName().equals(((ConfigurationClass) other).getMetadata().getClassName())));
    }

    @Override
    public int hashCode() {
        return getMetadata().getClassName().hashCode();
    }

    @Override
    public String toString() {
        return "ConfClass: bName=" + this.beanName + ", bMethodsSize=" + this.beanMethods.size();
    }

}
