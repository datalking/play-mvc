package com.github.datalking.context.annotation;

import com.github.datalking.annotation.Bean;
import com.github.datalking.annotation.ComponentScan;
import com.github.datalking.annotation.Import;
import com.github.datalking.annotation.meta.AnnotationAttributes;
import com.github.datalking.annotation.meta.AnnotationMetadata;
import com.github.datalking.annotation.meta.MethodMetadata;
import com.github.datalking.annotation.meta.StandardAnnotationMetadata;
import com.github.datalking.beans.factory.config.AnnotatedBeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.support.AbstractAutowireCapableBeanFactory;
import com.github.datalking.beans.factory.support.AbstractBeanDefinition;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.common.env.CompositePropertySource;
import com.github.datalking.common.env.Environment;
import com.github.datalking.common.env.PropertySource;
import com.github.datalking.common.env.StandardEnvironment;
import com.github.datalking.io.ResourcePropertySource;
import com.github.datalking.util.AnnoScanUtils;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * 解析带有@Configuration注解的类下的其他注解，如@Import, @EnableXxx
 *
 * @author yaoo on 4/13/18
 */
public class ConfigurationClassParser {

    private final BeanDefinitionRegistry registry;

    private final ComponentScanAnnotationParser componentScanParser;

    /**
     * 存放已经解析过的configClass，多是@Import注解指定的类，也可以是mvc、dao相关的bean
     */
    private final Map<ConfigurationClass, ConfigurationClass> configurationClasses = new LinkedHashMap<>();

    private final Stack<PropertySource<?>> propertySources = new Stack<>();

    private Environment environment = new StandardEnvironment();

    public ConfigurationClassParser(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.componentScanParser = new ComponentScanAnnotationParser(registry);

    }

    public void parse(Set<BeanDefinitionHolder> configCandidates) {

        for (BeanDefinitionHolder holder : configCandidates) {
            BeanDefinition bd = holder.getBeanDefinition();

            if (bd instanceof AnnotatedBeanDefinition) {
                parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
            }
        }
    }

    private void parse(AnnotationMetadata metadata, String beanName) {
        processConfigurationClass(new ConfigurationClass(metadata, beanName));
    }

    protected final void parse(Class<?> clazz, String beanName) {
        processConfigurationClass(new ConfigurationClass(clazz, beanName));
    }

    private void processConfigurationClass(ConfigurationClass configClass) {

//        ConfigurationClass existingClass = this.configurationClasses.get(configClass);
//        if (existingClass != null) {
//            this.configurationClasses.remove(configClass);
//        }

        // ==== 真正扫描@Configuration、@Bean、@ComponentScan todo 处理configClass的父类
        doProcessConfigurationClass(configClass);

        // 存储扫描结果
        this.configurationClasses.put(configClass, configClass);

    }


    /**
     * 扫描@ComponentScan、@Bean
     *
     * @param configClass 标注有@Configuration的类的元信息
     */
    private void doProcessConfigurationClass(ConfigurationClass configClass) {

        // 如果configClass标注有@PropertySource，则获取该注解的所有属性map
        Set<AnnotationAttributes> propertySources = attributesForRepeatable(
                configClass.getMetadata(), null, com.github.datalking.annotation.PropertySource.class);

        if (propertySources != null && !propertySources.isEmpty()) {

            /// 遍历注解属性值
            for (AnnotationAttributes propertySource : propertySources) {
                processPropertySource(propertySource);
            }
        }

        // 如果configClass标注有@ComponentScan，则获取该注解的所有属性map
        // Set<AnnotationAttributes> componentScans = attributesForRepeatable(configClass.getMetadata(), ComponentScans.class, ComponentScan.class);
        Set<AnnotationAttributes> componentScans = attributesForRepeatable(
                configClass.getMetadata(), null, ComponentScan.class);

        if (!componentScans.isEmpty()) {

            /// 遍历Set<AnnotationAttributes>
            for (AnnotationAttributes componentScan : componentScans) {
                // 通过ComponentScanAnnotationParser解析@ComponentScan注解，扫描指定包下的bean，一般是dao或mvc
                Set<BeanDefinitionHolder> scannedBeanDefinitions = this.componentScanParser.parse(componentScan, configClass.getMetadata().getClassName());

                // 递归处理各个扫描到的bean
                for (BeanDefinitionHolder holder : scannedBeanDefinitions) {

                    AbstractBeanDefinition bd = (AbstractBeanDefinition) holder.getBeanDefinition();
                    Class clazz = ((AbstractAutowireCapableBeanFactory) registry).doResolveBeanClass(bd);

                    // 将配置的各个包下的Component类扫描出来 full lite  todo递归中止条件
                    if (AnnoScanUtils.isCandidateComponent(clazz)) {

                        parse(clazz, holder.getBeanName());
                    }

                }
            }
        }

        // 获取configClass类上直接@Import导入的类，或元注解中包含@Import的类
        Set<ConfigurationClass> imports = getImports(configClass);
        if (imports != null && imports.size() > 0) {
            // 处理@Import导入的类，如@EnableWebMvc，@EnableAspectJAutoProxy
            processImports(configClass, imports);
        }

        // ==== 扫描@Bean
        Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(configClass);

        for (MethodMetadata methodMetadata : beanMethods) {
            configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
        }

        // 处理接口默认方法

        // 处理父类

    }

    private void processPropertySource(AnnotationAttributes propertySource) {

        String name = propertySource.getString("name");
        // 获取@PropertySource中value的值，即配置的属性文件路径
        String[] locations = propertySource.getStringArray("value");
        int locationCount = locations.length;
        if (locationCount == 0) {
            throw new IllegalArgumentException("At least one @PropertySource(value) location is required");
        }

        /// 遍历所有文件路径，解析文件名中的占位符
        for (int i = 0; i < locationCount; i++) {
            locations[i] = this.environment.resolveRequiredPlaceholders(locations[i]);
        }

//        ClassLoader classLoader = this.resourceLoader.getClassLoader();
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (!StringUtils.hasText(name)) {
            for (String location : locations) {
                this.propertySources.push(new ResourcePropertySource(location, classLoader));
            }
        } else {

            /// 如果仅指定了1个属性文件
            if (locationCount == 1) {
                this.propertySources.push(new ResourcePropertySource(name, locations[0], classLoader));
            }
            /// 如果指定了多个属性文件
            else {
                CompositePropertySource ps = new CompositePropertySource(name);
                for (int i = locations.length - 1; i >= 0; i--) {
                    ps.addPropertySource(new ResourcePropertySource(locations[i], classLoader));
                }
                this.propertySources.push(ps);
            }
        }
    }


    /**
     * 将@Import的类加入beanDefinitionMap
     * 包括@EnableAspectJAutoProxy
     *
     * @param configClass      含有@Configuration注解的类A
     * @param importCandidates 该类A上@Import注解指定的类
     */
    private void processImports(ConfigurationClass configClass, Collection<ConfigurationClass> importCandidates) {

        if (importCandidates.isEmpty()) {
            return;
        }

        /// 遍历@Import的类
        for (ConfigurationClass c : importCandidates) {
            Class curClass = ((StandardAnnotationMetadata) c.getMetadata()).getIntrospectedClass();

            /// 若该类是ImportBeanDefinitionRegistrar接口的实现类
            if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(curClass)) {
                ImportBeanDefinitionRegistrar obj = null;
                try {
                    obj = (ImportBeanDefinitionRegistrar) curClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                // 将@Import的class加入configClass的map，用于直接注册BeanDefinition
                configClass.addImportBeanDefinitionRegistrar(obj, c.getMetadata());
            } else {
                // 将该类作为普通@Configuration标注的类进行处理，添加到configurationClasses map
                processConfigurationClass(c);

            }


        }


    }

    /**
     * 获取@Import注解指定的类
     */
    private Set<ConfigurationClass> getImports(ConfigurationClass confClass) {
        Set<ConfigurationClass> imports = new LinkedHashSet<>();
        Set<Class> visited = new LinkedHashSet<>();

        Annotation[] annos = confClass.getMetadata().getAnnotations();

        Deque<AnnoClassTuple2> stack = new ArrayDeque<>();
        for (Annotation annotation : annos) {
            AnnoClassTuple2 annoClass = new AnnoClassTuple2(annotation, ((StandardAnnotationMetadata) confClass.getMetadata()).getIntrospectedClass());
            stack.push(annoClass);
        }
//        stack.addAll(Arrays.asList(annos));

        while (!stack.isEmpty()) {
            AnnoClassTuple2 ac = stack.pop();

            Class annoTypeClass = ac.getAnnotation().annotationType();

            if (annoTypeClass.getName().equals(Import.class.getName())) {

                Class<?>[] importedClass = ac.getClazz().getAnnotation(Import.class).value();

                for (Class c : importedClass) {
                    ConfigurationClass cc = new ConfigurationClass(c, ClassUtils.getCamelCaseNameFromClass(c));
                    cc.setImported(true);
                    imports.add(cc);
                }

            } else {
                visited.add(annoTypeClass);

                Annotation[] annos2 = annoTypeClass.getAnnotations();
                for (Annotation a : annos2) {
                    AnnoClassTuple2 ac2 = new AnnoClassTuple2(a, annoTypeClass);
                    if (!visited.contains(a.annotationType()) && !stack.contains(ac2)) {
                        stack.push(ac2);

                    }
                }

            }

        }

//        collectImports(confClass, imports, visited);
        return imports;
    }


//    private void collectImports(ConfigurationClass configClass, Set<ConfigurationClass> imports, Set<ConfigurationClass> visited) {
//
//        if (visited.add(configClass)) {
//
//            for (ConfigurationClass annotation : configClass.getAnnotations()) {
//
//                String annName = annotation.getMetadata().getClassName();
//                if (!annName.equals(Import.class.getName()) && !annName.startsWith("java")) {
//                    collectImports(annotation, imports, visited);
//                }
//            }
//
//            imports.addAll(configClass.getAnnotationAttributes(Import.class.getName(), "value"));
//        }
//
//    }

    private Set<MethodMetadata> retrieveBeanMethodMetadata(ConfigurationClass configClass) {

        AnnotationMetadata original = configClass.getMetadata();
        Set<MethodMetadata> beanMethods = original.getAnnotatedMethods(Bean.class);
        return beanMethods;


    }

    public Set<ConfigurationClass> getConfigurationClasses() {
        return this.configurationClasses.keySet();
    }


    private Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
                                                              Class<?> containerClass,
                                                              Class<?> annotationClass) {

        //String containerClassName = containerClass.getName();
        // String annotationClassName = annotationClass.getName();

        Set<AnnotationAttributes> result = new LinkedHashSet<>();

        AnnotationAttributes attrMap = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationClass, false));
        if (attrMap != null) {
            result.add(attrMap);
        }


        //Map<String, Object> container = metadata.getAnnotationAttributes(containerClassName,false);

//        if (container != null && container.containsKey("value")) {
//            for (Map<String, Object> containedAttributes : (Map<String, Object>[]) container.get("value")) {
//                result.add(AnnotationAttributes.fromMap(containedAttributes));
//            }
//        }

        return result;
    }


}
