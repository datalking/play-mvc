package com.github.datalking.mapper;

import com.github.datalking.SqlSessionTemplate;
import com.github.datalking.beans.PropertyValue;
import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.factory.BeanNameAware;
import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.config.PropertyResourceConfigurer;
import com.github.datalking.beans.factory.config.TypedStringValue;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistry;
import com.github.datalking.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import com.github.datalking.beans.factory.support.BeanNameGenerator;
import com.github.datalking.beans.factory.support.DefaultListableBeanFactory;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.util.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

import static com.github.datalking.util.Assert.notNull;

/**
 * 扫描指定包下的Mapper接口并注册
 */
public class MapperScannerConfigurer
        implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

    // 指定扫描的包，可以包含多个通过逗号分隔的包名，这些指定的包以及其子包都会被扫描
    private String basePackage;

    private boolean addToConfig = true;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private String sqlSessionFactoryBeanName;

    private String sqlSessionTemplateBeanName;

    // annotationClass字段不为null时，则只注册通过annotationClass注解标记的接口
    private Class<? extends Annotation> annotationClass;

    // markerinterface字段不为null时，则只注册继承自markerInterface的接口，若annotationClass和markerInterface都不为null，则取并集
    private Class<?> markerInterface;

    private ApplicationContext applicationContext;

    private String beanName;

    private boolean processPropertyPlaceHolders;

    private BeanNameGenerator nameGenerator;

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Same as {@code MapperFactoryBean#setAddToConfig(boolean)}.
     *
     * @param addToConfig mapper接口是否添加到mybatis
     */
    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    /**
     * This property specifies the annotation that the scanner will search for.
     * The scanner will register all interfaces in the base package that also have the specified annotation.
     * Note this can be combined with markerInterface.
     *
     * @param annotationClass annotation class
     */
    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * This property specifies the parent that the scanner will search for.
     * The scanner will register all interfaces in the base package that also have the specified interface class as a parent.
     * Note this can be combined with annotationClass.
     *
     * @param superClass parent class
     */
    public void setMarkerInterface(Class<?> superClass) {
        this.markerInterface = superClass;
    }

    /**
     * Specifies which {@code SqlSessionTemplate} to use in the case that there is more than one in the spring context.
     * Usually this is only needed when you have more than one datasource.
     * <p>
     *
     * @param sqlSessionTemplate
     * @deprecated Use {@link #setSqlSessionTemplateBeanName(String)} instead
     */
    @Deprecated
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * Specifies which {@code SqlSessionTemplate} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     * <p>
     * Note bean names are used, not bean references. This is because the scanner
     * loads early during the start process and it is too early to build mybatis
     * object instances.
     *
     * @param sqlSessionTemplateName Bean name of the {@code SqlSessionTemplate}
     * @since 1.1.0
     */
    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateName;
    }

    /**
     * Specifies which {@code SqlSessionFactory} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     * <p>
     *
     * @param sqlSessionFactory
     * @deprecated Use {@link #setSqlSessionFactoryBeanName(String)} instead.
     */
    @Deprecated
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Specifies which {@code SqlSessionFactory} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     * <p>
     * Note bean names are used, not bean references. This is because the scanner
     * loads early during the start process and it is too early to build mybatis
     * object instances.
     *
     * @param sqlSessionFactoryName Bean name of the {@code SqlSessionFactory}
     * @since 1.1.0
     */
    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryName;
    }


    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public BeanNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(BeanNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    @Override
    public void afterPropertiesSet() {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // left intentionally blank
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {

        if (this.processPropertyPlaceHolders) {

            // 处理applicationContext.xml文件中MapperScannerConfigurer配置的占位符
            processPropertyPlaceHolders();
        }

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setAddToConfig(this.addToConfig);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setSqlSessionFactory(this.sqlSessionFactory);
        scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
        scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
        scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);

        // 根据上面的配置，生成相应的过滤器，这些过滤器在扫描过程中会过滤掉不符合添加的内容
        scanner.registerFilters();

        // 开始扫描 basePackage 字段中指定的包及其子包
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    /*
     * BeanDefinitionRegistries are called early in application startup, before
     * BeanFactoryPostProcessors. This means that PropertyResourceConfigurers will not have been
     * loaded and any property substitution of this class' properties will fail. To avoid this, find
     * any PropertyResourceConfigurers defined in the context and run them on this class' bean
     * definition. Then update the values.
     */
    private void processPropertyPlaceHolders() {
        Map<String, PropertyResourceConfigurer> prcs = applicationContext.getBeansOfType(PropertyResourceConfigurer.class);

        if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext)
                    .getBeanFactory()
                    .getBeanDefinition(beanName);

            // PropertyResourceConfigurer does not expose any methods to explicitly perform
            // property placeholder substitution. Instead, create a BeanFactory that just
            // contains this mapper scanner and post process the factory.
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);

            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }

            PropertyValues values = mapperScannerBean.getPropertyValues();

            this.basePackage = updatePropertyValue("basePackage", values);
            this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
            this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
        }
    }

    private String updatePropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);

        if (property == null) {
            return null;
        }

        Object value = property.getValue();

        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else {
            return null;
        }
    }

}
