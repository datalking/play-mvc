package com.github.datalking.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注要在哪些包下扫描Mapper接口
 * Use this annotation to register MyBatis mapper interfaces when using Java Config.
 * It performs when same work as MapperScannerConfigurer via {@link MapperScannerRegistrar}.
 *
 * <p>Configuration example:</p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;MapperScan("org.mybatis.spring.sample.mapper")
 * public class AppConfig {
 *
 *   &#064;Bean
 *   public DataSource dataSource() {
 *     return new EmbeddedDatabaseBuilder()
 *              .addScript("schema.sql")
 *              .build();
 *   }
 *
 *   &#064;Bean
 *   public DataSourceTransactionManager transactionManager() {
 *     return new DataSourceTransactionManager(dataSource());
 *   }
 *
 *   &#064;Bean
 *   public SqlSessionFactory sqlSessionFactory() throws Exception {
 *     SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
 *     sessionFactory.setDataSource(dataSource());
 *     return sessionFactory.getObject();
 *   }
 * }
 * </pre>
 *
 * @author Michael Lanyon
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MapperScannerRegistrar.class)
public @interface MapperScan {

    /**
     * Alias for the {@link #basePackages()} attribute.
     * Allows for more concise annotation declarations e.g.:
     * {@code @EnableMyBatisMapperScanner("org.my.pkg")} instead of
     * {@code @EnableMyBatisMapperScanner(basePackages= "org.my.pkg"})}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for MyBatis interfaces.
     * Note that only interfaces with at least one method will be registered;
     * concrete classes will be ignored.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * <p>Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * This property specifies the annotation that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also have
     * the specified annotation.
     * <p>
     * Note this can be combined with markerInterface.
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * This property specifies the parent that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also have
     * the specified interface class as a parent.
     * <p>
     * Note this can be combined with annotationClass.
     */
    Class<?> markerInterface() default Class.class;

    /**
     * Specifies which {@code SqlSessionTemplate} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     */
    String sqlSessionTemplateRef() default "";

    /**
     * Specifies which {@code SqlSessionFactory} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     */
    String sqlSessionFactoryRef() default "";

    /**
     * Specifies a custom MapperFactoryBean to return a mybatis proxy as spring bean.
     */
    Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

}
