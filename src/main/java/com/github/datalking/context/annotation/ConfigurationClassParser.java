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
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yaoo on 4/13/18
 */
public class ConfigurationClassParser {

    private final BeanDefinitionRegistry registry;

    private final ComponentScanAnnotationParser componentScanParser;

    private final Map<ConfigurationClass, ConfigurationClass> configurationClasses = new LinkedHashMap<>();


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

        ConfigurationClass existingClass = this.configurationClasses.get(configClass);

//        if (existingClass != null) {
//            this.configurationClasses.remove(configClass);
//        }

        // ==== 真正扫描@Configuration、@Bean、@ComponentScan
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

        // ==== 如果configClass标注有@ComponentScan，则获取注解的属性map
//        Set<AnnotationAttributes> componentScans = attributesForRepeatable(configClass.getMetadata(), ComponentScans.class, ComponentScan.class);
        Set<AnnotationAttributes> componentScans = attributesForRepeatable(configClass.getMetadata(), null, ComponentScan.class);

        if (!componentScans.isEmpty()) {
            for (AnnotationAttributes componentScan : componentScans) {

                // 通过ComponentScanAnnotationParser解析@ComponentScan注解
                Set<BeanDefinitionHolder> scannedBeanDefinitions = this.componentScanParser.parse(componentScan, configClass.getMetadata().getClassName());

//                for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
//
//                    AbstractBeanDefinition bd = (AbstractBeanDefinition) holder.getBeanDefinition();
//
//                    Class clazz = ((AbstractAutowireCapableBeanFactory) registry).doResolveBeanClass(bd);
//
//                    // 将配置的各个包下的Component类扫描出来 full lite  todo递归中止条件
//                    if (clazz.isAnnotationPresent(Component.class)) {
//                        parse(clazz, holder.getBeanName());
//                    }
//
//                }
            }
        }


        Set<ConfigurationClass> imports = getImports(configClass);
        // 循环处理注解中含有@import的注解
        processImports(configClass, imports);


        // 循环处理注解中含有@import的类
        //processImports(configClass, getImports(configClass), true);

        // ==== 扫描@Bean
        Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(configClass);

        for (MethodMetadata methodMetadata : beanMethods) {
            configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
        }


    }

    private void processImports(ConfigurationClass configClass, Collection<ConfigurationClass> importCandidates) {

        if (importCandidates.isEmpty()) {
            return;
        }

        for (ConfigurationClass c : importCandidates) {
            Class curClass = ((StandardAnnotationMetadata) c.getMetadata()).getIntrospectedClass();
            ImportBeanDefinitionRegistrar obj = null;
            if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(curClass)) {
                try {
                    obj = (ImportBeanDefinitionRegistrar) curClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            // 将@Import的class加入configClass的map
            configClass.addImportBeanDefinitionRegistrar(obj, c.getMetadata());

        }


    }


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
                    imports.add(new ConfigurationClass(c, c.getSimpleName()));
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
