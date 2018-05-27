package com.github.datalking.context.annotation;

import com.github.datalking.annotation.Component;
import com.github.datalking.beans.factory.config.AnnotatedBeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.BeanDefinitionHolder;
import com.github.datalking.beans.factory.support.AnnotatedGenericBeanDefinition;
import com.github.datalking.beans.factory.support.BeanDefinitionReaderUtils;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.datalking.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;
import static com.github.datalking.util.AnnoScanUtils.getAnnoClassIncludingSuper;
import static com.github.datalking.util.ClassUtils.getCamelCaseNameFromClass;

/**
 * 基于路径的BeanDefinition扫描器
 *
 * @author yaoo on 4/9/18
 */
public class ClassPathBeanDefinitionScanner {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BeanDefinitionRegistry registry;

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this(registry, true);
    }

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        this.registry = registry;
    }


    public void scan(String... basePackages) {

        doScan(basePackages);

    }

    /**
     * 执行扫描带有@Component标注的bean
     *
     * @param basePackages 要扫描的包
     * @return beanDefinition的集合
     */
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {

        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();

        for (String basePackage : basePackages) {

            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);

            for (BeanDefinition candidate : candidates) {

                String beanName = BeanDefinitionReaderUtils.generateAnnotatedBeanName((AnnotatedBeanDefinition) candidate, this.registry);
//                if (candidate instanceof AbstractBeanDefinition) {
//                    postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
//                }
//                if (candidate instanceof AnnotatedBeanDefinition) {
//                    AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
//                }

                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                beanDefinitions.add(definitionHolder);

                registerBeanDefinition(definitionHolder, this.registry);
            }
        }

        return beanDefinitions;

    }

    /**
     * 获取指定包下的.class文件，生成BeanDefinition
     *
     * @param basePackage 指定包全限定名
     * @return 包下所有class对应的BeanDefinition
     */
    public Set<BeanDefinition> findCandidateComponents(String basePackage) {

        Set<BeanDefinition> candidates = new LinkedHashSet<>();

        Set<Class> classSet = ResourceUtils.getAllClassFromPackage(basePackage, true);

        for (Class c : classSet) {

            if (isCandidateComponent(c)) {
                AnnotatedBeanDefinition abd = new AnnotatedGenericBeanDefinition(c);
                candidates.add(abd);
            }
        }

        return candidates;

    }

    /**
     * 判断class上是否有 @Component 注解
     * 对应于spring的ClassPathScanningCandidateComponentProvider.isCandidateComponent()
     *
     * @param clazz 类对象
     * @return 是否有
     */
    protected boolean isCandidateComponent(Class clazz) {

        /// 忽略已注册过的bean
        if (this.registry.containsBeanDefinition(getCamelCaseNameFromClass(clazz))) {
            return false;
        }

        /// 判断class上直接有@Component
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }

        /// 判断class上的注解的注解包含@Component，如@Controller
        Set<Class> annoAll = getAnnoClassIncludingSuper(clazz);
        if (annoAll != null) {
            for (Class c : annoAll) {
                if (c.getName().equals(Component.class.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if (!this.registry.containsBeanDefinition(beanName)) {
            return true;
        } else {
            BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
//            BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
            BeanDefinition originatingDef = null;
            if (originatingDef != null) {
                existingDef = originatingDef;
            }

            if (this.isCompatible(beanDefinition, existingDef)) {
                return false;
            } else {
                throw new IllegalStateException("Annotation-specified bean name '" + beanName + "' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
            }
        }
    }

    protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
//        return !(existingDefinition instanceof ScannedGenericBeanDefinition) ||
//                newDefinition.getSource().equals(existingDefinition.getSource()) ||
//                newDefinition.equals(existingDefinition);
        return newDefinition.equals(existingDefinition);
    }

    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }

}
