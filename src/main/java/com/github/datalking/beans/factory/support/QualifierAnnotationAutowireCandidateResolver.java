package com.github.datalking.beans.factory.support;

import com.github.datalking.annotation.Qualifier;
import com.github.datalking.annotation.Value;
import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.config.DependencyDescriptor;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.convert.SimpleTypeConverter;
import com.github.datalking.exception.NoSuchBeanDefinitionException;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 获取@Qualifier、@Value注解的值进行autowire
 * 默认使用此实现类，在AnnotationConfigUtils中实例化
 *
 * @author yaoo on 5/29/18
 */
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryAware {

    private final Set<Class<? extends Annotation>> qualifierTypes = new LinkedHashSet<>();

    private Class<? extends Annotation> valueAnnotationType = Value.class;

    private BeanFactory beanFactory;

    public QualifierAnnotationAutowireCandidateResolver() {
        this.qualifierTypes.add(Qualifier.class);
        try {
            this.qualifierTypes.add(
                    (Class<? extends Annotation>) ClassUtils.forName("javax.inject.Qualifier",
                            QualifierAnnotationAutowireCandidateResolver.class.getClassLoader()));
        } catch (ClassNotFoundException ex) {
            // JSR-330 API not available - simply skip.
        }
    }

    public QualifierAnnotationAutowireCandidateResolver(Class<? extends Annotation> qualifierType) {
        Assert.notNull(qualifierType, "'qualifierType' must not be null");
        this.qualifierTypes.add(qualifierType);
    }

    public QualifierAnnotationAutowireCandidateResolver(Set<Class<? extends Annotation>> qualifierTypes) {
        Assert.notNull(qualifierTypes, "'qualifierTypes' must not be null");
        this.qualifierTypes.addAll(qualifierTypes);
    }

    public void addQualifierType(Class<? extends Annotation> qualifierType) {
        this.qualifierTypes.add(qualifierType);
    }

    public void setValueAnnotationType(Class<? extends Annotation> valueAnnotationType) {
        this.valueAnnotationType = valueAnnotationType;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {

        if (!bdHolder.getBeanDefinition().isAutowireCandidate()) {
            // if explicitly false, do not proceed with qualifier check
            return false;
        }

        if (descriptor == null) {
            // no qualification necessary
            return true;
        }
        /// todo 简化规则
        boolean match = checkQualifiers(bdHolder, descriptor.getAnnotations());
        if (match) {
            MethodParameter methodParam = descriptor.getMethodParameter();
            if (methodParam != null) {
                Method method = methodParam.getMethod();
                if (method == null || void.class.equals(method.getReturnType())) {
                    match = checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
                }
            }
        }
        return match;
    }

    /**
     * Match the given qualifier annotations against the candidate bean definition.
     */
    protected boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotationsToSearch) {
        if (ObjectUtils.isEmpty(annotationsToSearch)) {
            return true;
        }
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        for (Annotation annotation : annotationsToSearch) {
            Class<? extends Annotation> type = annotation.annotationType();
            boolean checkMeta = true;
            boolean fallbackToMeta = false;
            if (isQualifier(type)) {
                if (!checkQualifier(bdHolder, annotation, typeConverter)) {
                    fallbackToMeta = true;
                } else {
                    checkMeta = false;
                }
            }
            if (checkMeta) {
                boolean foundMeta = false;
                for (Annotation metaAnn : type.getAnnotations()) {
                    Class<? extends Annotation> metaType = metaAnn.annotationType();
                    if (isQualifier(metaType)) {
                        foundMeta = true;
                        // Only accept fallback match if @Qualifier annotation has a value...
                        // Otherwise it is just a marker for a custom qualifier annotation.
                        if ((fallbackToMeta && StringUtils.isEmpty(AnnotationUtils.getValue(metaAnn))) ||
                                !checkQualifier(bdHolder, metaAnn, typeConverter)) {
                            return false;
                        }
                    }
                }
                if (fallbackToMeta && !foundMeta) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean isQualifier(Class<? extends Annotation> annotationType) {
        for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
            if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkQualifier(BeanDefinitionHolder bdHolder, Annotation annotation, TypeConverter typeConverter) {

        Class<? extends Annotation> type = annotation.annotationType();
        RootBeanDefinition bd = (RootBeanDefinition) bdHolder.getBeanDefinition();

        AutowireCandidateQualifier qualifier = bd.getQualifier(type.getName());

        if (qualifier == null) {
            qualifier = bd.getQualifier(ClassUtils.getShortName(type));
        }
        if (qualifier == null) {
            // First, check annotation on factory method, if applicable
            Annotation targetAnnotation = getFactoryMethodAnnotation(bd, type);
            if (targetAnnotation == null) {
                RootBeanDefinition dbd = getResolvedDecoratedDefinition(bd);
                if (dbd != null) {
                    targetAnnotation = getFactoryMethodAnnotation(dbd, type);
                }
            }
            if (targetAnnotation == null) {
                // Look for matching annotation on the target class
                if (this.beanFactory != null) {
                    try {
                        Class<?> beanType = this.beanFactory.getType(bdHolder.getBeanName());
                        if (beanType != null) {
                            targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(beanType), type);
                        }
                    } catch (NoSuchBeanDefinitionException ex) {
                        // Not the usual case - simply forget about the type check...
                    }
                }
                if (targetAnnotation == null && bd.hasBeanClass()) {
                    targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(bd.getBeanClass()), type);
                }
            }
            if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
                return true;
            }
        }

        Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
        if (attributes.isEmpty() && qualifier == null) {
            // If no attributes, the qualifier must be present
            return false;
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = null;
            // Check qualifier first
            if (qualifier != null) {
                actualValue = qualifier.getAttribute(attributeName);
            }
            if (actualValue == null) {
                // Fall back on bean definition attribute
                actualValue = bd.getAttribute(attributeName);
            }
            if (actualValue == null && attributeName.equals(AutowireCandidateQualifier.VALUE_KEY) &&
                    expectedValue instanceof String && bdHolder.matchesName((String) expectedValue)) {
                // Fall back on bean name (or alias) match
                continue;
            }
            if (actualValue == null && qualifier != null) {
                // Fall back on default, but only if the qualifier is present
                actualValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
            }
            if (actualValue != null) {
                actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
            }
            if (!expectedValue.equals(actualValue)) {
                return false;
            }
        }
        return true;
    }

    protected RootBeanDefinition getResolvedDecoratedDefinition(RootBeanDefinition rbd) {
        BeanDefinitionHolder decDef = rbd.getDecoratedDefinition();
        if (decDef != null && this.beanFactory instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory clbf = (ConfigurableListableBeanFactory) this.beanFactory;
            if (clbf.containsBeanDefinition(decDef.getBeanName())) {
//                BeanDefinition dbd = clbf.getMergedBeanDefinition(decDef.getBeanName());
                BeanDefinition dbd = clbf.getMergedBeanDefinition(decDef.getBeanName());
                if (dbd instanceof RootBeanDefinition) {
                    return (RootBeanDefinition) dbd;
                }
            }
        }
        return null;
    }

    protected Annotation getFactoryMethodAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {

        Method resolvedFactoryMethod = bd.getResolvedFactoryMethod();

        return (resolvedFactoryMethod != null ? AnnotationUtils.getAnnotation(resolvedFactoryMethod, type) : null);
    }


    /**
     * 检查依赖项的注解是否使用@Value指定了值
     * 返回@Vaule注解的值，可能含有占位符
     * Determine whether the given dependency carries a value annotation.
     */
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        // 先查找@Value注解的字段
        Object value = findValue(descriptor.getAnnotations());
        /// 若value为空，再查找方法参数
        if (value == null) {
            MethodParameter methodParam = descriptor.getMethodParameter();
            if (methodParam != null) {

                value = findValue(methodParam.getMethodAnnotations());
            }
        }

        return value;
    }

    /**
     * Determine a suggested value from any of the given candidate annotations.
     */
    protected Object findValue(Annotation[] annotationsToSearch) {

        for (Annotation annotation : annotationsToSearch) {

            /// 如果是@Value注解
            if (this.valueAnnotationType.isInstance(annotation)) {

                // ==== 提取@Value注解的值
                return extractValue(annotation);
            }
        }

        for (Annotation annotation : annotationsToSearch) {
            /// 如果是包含@Value注解的注解
            Annotation metaAnn = annotation.annotationType().getAnnotation(this.valueAnnotationType);
            if (metaAnn != null) {

                // ==== 提取@Value注解的值
                return extractValue(metaAnn);
            }
        }

        return null;
    }

    protected Object extractValue(Annotation valueAnnotation) {

        Object value = AnnotationUtils.getValue(valueAnnotation);

        if (value == null) {
            throw new IllegalStateException("Value annotation must have a value attribute");
        }
        return value;
    }

}
