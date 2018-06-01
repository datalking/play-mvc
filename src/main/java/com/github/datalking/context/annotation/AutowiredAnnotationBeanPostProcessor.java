package com.github.datalking.context.annotation;

import com.github.datalking.annotation.Autowired;
import com.github.datalking.annotation.Value;
import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.BeanFactoryUtils;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.config.DependencyDescriptor;
import com.github.datalking.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import com.github.datalking.beans.factory.config.RuntimeBeanReference;
import com.github.datalking.beans.factory.support.MergedBeanDefinitionPostProcessor;
import com.github.datalking.beans.factory.support.RootBeanDefinition;
import com.github.datalking.common.BridgeMethodResolver;
import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.Ordered;
import com.github.datalking.common.PriorityOrdered;
import com.github.datalking.exception.BeanCreationException;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.util.BeanUtils;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动注入bean到字段和setter方法
 * 会解析@Autowired和@Value
 *
 * @author yaoo on 5/28/18
 */
public class AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>();

    private String requiredParameterName = "required";

    private boolean requiredParameterValue = true;

    private int order = Ordered.LOWEST_PRECEDENCE - 2;

    private ConfigurableListableBeanFactory beanFactory;

    private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(64);

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(64);

    @SuppressWarnings("unchecked")
    public AutowiredAnnotationBeanPostProcessor() {
        this.autowiredAnnotationTypes.add(Autowired.class);
        this.autowiredAnnotationTypes.add(Value.class);
        try {

            this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
                    ClassUtils.forName("javax.inject.Inject", AutowiredAnnotationBeanPostProcessor.class.getClassLoader())
            );

            logger.info("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
        } catch (ClassNotFoundException ex) {
            // JSR-330 API not available - simply skip.
        }
    }

    public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
        Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
        this.autowiredAnnotationTypes.clear();
        this.autowiredAnnotationTypes.add(autowiredAnnotationType);
    }

    public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
        Assert.notEmpty(autowiredAnnotationTypes.toArray(), "'autowiredAnnotationTypes' must not be empty");
        this.autowiredAnnotationTypes.clear();
        this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
    }

    public void setRequiredParameterName(String requiredParameterName) {
        this.requiredParameterName = requiredParameterName;
    }

    public void setRequiredParameterValue(boolean requiredParameterValue) {
        this.requiredParameterValue = requiredParameterValue;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                    "AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }


    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) {
        // Quick check on the concurrent map first, with minimal locking.
        Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
        if (candidateConstructors == null) {
            synchronized (this.candidateConstructorsCache) {
                candidateConstructors = this.candidateConstructorsCache.get(beanClass);
                if (candidateConstructors == null) {
                    Constructor<?>[] rawCandidates = beanClass.getDeclaredConstructors();
                    List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
                    Constructor<?> requiredConstructor = null;
                    Constructor<?> defaultConstructor = null;
                    for (Constructor<?> candidate : rawCandidates) {
                        Annotation ann = findAutowiredAnnotation(candidate);
                        if (ann != null) {
                            if (requiredConstructor != null) {
                                try {
                                    throw new Exception(beanName + "Invalid autowire-marked constructor: " + candidate + ". Found constructor with 'required' Autowired annotation already: " + requiredConstructor);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (candidate.getParameterTypes().length == 0) {
                                throw new IllegalStateException(
                                        "Autowired annotation requires at least one argument: " + candidate);
                            }
                            boolean required = determineRequiredStatus(ann);
                            if (required) {
                                if (!candidates.isEmpty()) {
                                    try {
                                        throw new Exception(beanName + "Invalid autowire-marked constructors: " + candidates + ". Found constructor with 'required' Autowired annotation: " + candidate);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                requiredConstructor = candidate;
                            }
                            candidates.add(candidate);
                        } else if (candidate.getParameterTypes().length == 0) {
                            defaultConstructor = candidate;
                        }
                    }
                    if (!candidates.isEmpty()) {
                        // Add default constructor to list of optional constructors, as fallback.
                        if (requiredConstructor == null) {
                            if (defaultConstructor != null) {
                                candidates.add(defaultConstructor);
                            } else if (candidates.size() == 1 && logger.isWarnEnabled()) {
                                logger.warn("Inconsistent constructor declaration on bean with name '" + beanName +
                                        "': single autowire-marked constructor flagged as optional - this constructor " +
                                        "is effectively required since there is no default constructor to fall back to: " +
                                        candidates.get(0));
                            }
                        }
                        candidateConstructors = candidates.toArray(new Constructor<?>[candidates.size()]);
                    } else {
                        candidateConstructors = new Constructor<?>[0];
                    }
                    this.candidateConstructorsCache.put(beanClass, candidateConstructors);
                }
            }
        }
        return (candidateConstructors.length > 0 ? candidateConstructors : null);
    }

    /**
     * 解析属性占位符的入口，并注入属性
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs,
                                                    PropertyDescriptor[] pds,
                                                    Object bean, String beanName) {

        // 获取beanName中依赖的其他bean，一般是带有@Autowired的字段
        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {

            // ==== 实现autowire的入口，将metadata中的依赖项注入bean的字段
            metadata.inject(bean, beanName, pvs);
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
        }

        return pvs;
    }

    public void processInjection(Object bean) {
        Class<?> clazz = bean.getClass();
        InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
        try {
            metadata.inject(bean, null, null);
        } catch (Throwable ex) {
            throw new BeanCreationException("Injection of autowired dependencies failed for class [" + clazz + "]", ex);
        }
    }


    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {

        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());

        // 从缓存中查找bean的依赖信息
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);

        /// 默认不需要refresh
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }

        return metadata;
    }

    private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
        LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<>();
        Class<?> targetClass = clazz;

        do {
            LinkedList<InjectionMetadata.InjectedElement> currElements = new LinkedList<>();
            for (Field field : targetClass.getDeclaredFields()) {
                Annotation ann = findAutowiredAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Autowired annotation is not supported on static fields: " + field);
                        }
                        continue;
                    }
                    boolean required = determineRequiredStatus(ann);
                    currElements.add(new AutowiredFieldElement(field, required));
                }
            }
            for (Method method : targetClass.getDeclaredMethods()) {
                Annotation ann = null;
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    ann = findAutowiredAnnotation(bridgedMethod);
                }
                if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Autowired annotation is not supported on static methods: " + method);
                        }
                        continue;
                    }
                    if (method.getParameterTypes().length == 0) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Autowired annotation should be used on methods with actual parameters: " + method);
                        }
                    }
                    boolean required = determineRequiredStatus(ann);
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                    currElements.add(new AutowiredMethodElement(method, required, pd));
                }
            }
            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    private Annotation findAutowiredAnnotation(AccessibleObject ao) {
        for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
            Annotation ann = AnnotationUtils.getAnnotation(ao, type);
            if (ann != null) {
                return ann;
            }
        }
        return null;
    }

    protected <T> Map<String, T> findAutowireCandidates(Class<T> type) {
        if (this.beanFactory == null) {
            throw new IllegalStateException("No BeanFactory configured - " +
                    "override the getBeanOfType method or specify the 'beanFactory' property");
        }
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
    }

    protected boolean determineRequiredStatus(Annotation ann) {
        try {
            Method method = ReflectionUtils.findMethod(ann.annotationType(), this.requiredParameterName);
            if (method == null) {
                // Annotations like @Inject and @Value don't have a method (attribute) named "required"
                // -> default to required status
                return true;
            }
            return (this.requiredParameterValue == (Boolean) ReflectionUtils.invokeMethod(method, ann));
        } catch (Exception ex) {
            // An exception was thrown during reflective invocation of the required attribute
            // -> default to required status
            return true;
        }
    }

    private void registerDependentBeans(String beanName, Set<String> autowiredBeanNames) {
        if (beanName != null) {
            for (String autowiredBeanName : autowiredBeanNames) {
                if (this.beanFactory.containsBean(autowiredBeanName)) {
                    this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
                }

            }
        }
    }

    private Object resolvedCachedArgument(String beanName, Object cachedArgument) {

        if (cachedArgument instanceof DependencyDescriptor) {
            DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
            TypeConverter typeConverter = this.beanFactory.getTypeConverter();
            return this.beanFactory.resolveDependency(descriptor, beanName, null, typeConverter);
        } else if (cachedArgument instanceof RuntimeBeanReference) {

            return this.beanFactory.getBean(((RuntimeBeanReference) cachedArgument).getBeanName());
        } else {

            return cachedArgument;
        }
    }

    /**
     * 标注有@AutoWired注解的字段元信息
     * Class representing injection information about an annotated field.
     */
    private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

        private final boolean required;

        private volatile boolean cached = false;

        private volatile Object cachedFieldValue;

        public AutowiredFieldElement(Field field, boolean required) {
            super(field, null);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Field field = (Field) this.member;

            try {
                Object value;

                if (this.cached) {

                    value = resolvedCachedArgument(beanName, this.cachedFieldValue);
                } else {

                    DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
                    Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
                    TypeConverter typeConverter = beanFactory.getTypeConverter();

                    // ==== 获取依赖字段的对象，实现autowire字段的入口
                    value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);

                    /// 加入缓存
                    synchronized (this) {
                        if (!this.cached) {
                            if (value != null || this.required) {
                                this.cachedFieldValue = desc;
                                registerDependentBeans(beanName, autowiredBeanNames);
                                if (autowiredBeanNames.size() == 1) {
                                    String autowiredBeanName = autowiredBeanNames.iterator().next();
                                    if (beanFactory.containsBean(autowiredBeanName)) {
                                        if (beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
                                            this.cachedFieldValue = new RuntimeBeanReference(autowiredBeanName);
                                        }
                                    }
                                }
                            } else {
                                this.cachedFieldValue = null;
                            }
                            this.cached = true;
                        }
                    }
                }

                /// 设置字段的值，即完成属性注入
                if (value != null) {
                    ReflectionUtils.makeAccessible(field);
                    field.set(bean, value);
                }
            } catch (Throwable ex) {
                throw new BeanCreationException("Could not autowire field: " + field, ex);
            }
        }
    }


    /**
     * 标注有@AutoWired注解的方法元信息
     * Class representing injection information about an annotated method.
     */
    private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

        private final boolean required;

        private volatile boolean cached = false;

        private volatile Object[] cachedMethodArguments;

        public AutowiredMethodElement(Method method, boolean required, PropertyDescriptor pd) {
            super(method, pd);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            if (checkPropertySkipping(pvs)) {
                return;
            }
            Method method = (Method) this.member;
            try {
                Object[] arguments;
                if (this.cached) {
                    // Shortcut for avoiding synchronization...
                    arguments = resolveCachedArguments(beanName);
                } else {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    arguments = new Object[paramTypes.length];
                    DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length];
                    Set<String> autowiredBeanNames = new LinkedHashSet<>(paramTypes.length);
                    TypeConverter typeConverter = beanFactory.getTypeConverter();
                    for (int i = 0; i < arguments.length; i++) {
                        MethodParameter methodParam = new MethodParameter(method, i);
                        GenericTypeResolver.resolveParameterType(methodParam, bean.getClass());
                        descriptors[i] = new DependencyDescriptor(methodParam, this.required);
                        arguments[i] = beanFactory.resolveDependency(
                                descriptors[i], beanName, autowiredBeanNames, typeConverter);
                        if (arguments[i] == null && !this.required) {
                            arguments = null;
                            break;
                        }
                    }
                    synchronized (this) {
                        if (!this.cached) {
                            if (arguments != null) {
                                this.cachedMethodArguments = new Object[arguments.length];
                                for (int i = 0; i < arguments.length; i++) {
                                    this.cachedMethodArguments[i] = descriptors[i];
                                }
                                registerDependentBeans(beanName, autowiredBeanNames);
                                if (autowiredBeanNames.size() == paramTypes.length) {
                                    Iterator<String> it = autowiredBeanNames.iterator();
                                    for (int i = 0; i < paramTypes.length; i++) {
                                        String autowiredBeanName = it.next();
                                        if (beanFactory.containsBean(autowiredBeanName)) {
                                            if (beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
                                                this.cachedMethodArguments[i] = new RuntimeBeanReference(autowiredBeanName);
                                            }
                                        }
                                    }
                                }
                            } else {
                                this.cachedMethodArguments = null;
                            }
                            this.cached = true;
                        }
                    }
                }
                if (arguments != null) {
                    ReflectionUtils.makeAccessible(method);
                    method.invoke(bean, arguments);
                }
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            } catch (Throwable ex) {
                throw new BeanCreationException("Could not autowire method: " + method, ex);
            }
        }

        private Object[] resolveCachedArguments(String beanName) {
            if (this.cachedMethodArguments == null) {
                return null;
            }
            Object[] arguments = new Object[this.cachedMethodArguments.length];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = resolvedCachedArgument(beanName, this.cachedMethodArguments[i]);
            }
            return arguments;
        }
    }

}
