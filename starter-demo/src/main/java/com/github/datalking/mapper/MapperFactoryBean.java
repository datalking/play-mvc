package com.github.datalking.mapper;

import com.github.datalking.beans.factory.FactoryBean;
import com.github.datalking.support.SqlSessionDaoSupport;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;

import static com.github.datalking.util.Assert.notNull;


/**
 * 自动创建Mapper接口实现类的代理对象
 * 直接将Mapper接口注入到Service层的Bean中，可以避免编写DAO层的实现类，直接使用Mapper接口对象
 * 包含afterPropertiesSet()方法
 * BeanFactory that enables injection of MyBatis mapper interfaces.
 * It can be set up with a SqlSessionFactory or a pre-configured SqlSessionTemplate.
 * <p>
 * Sample configuration:
 *
 * <pre class="code">
 * {@code
 *   <bean id="baseMapper" class="org.mybatis.spring.mapper.MapperFactoryBean" abstract="true" lazy-init="true">
 *     <property name="sqlSessionFactory" ref="sqlSessionFactory" />
 *   </bean>
 *
 *   <bean id="oneMapper" parent="baseMapper">
 *     <property name="mapperInterface" value="my.package.MyMapperInterface" />
 *   </bean>
 *
 *   <bean id="anotherMapper" parent="baseMapper">
 *     <property name="mapperInterface" value="my.package.MyAnotherMapperInterface" />
 *   </bean>
 * }
 * </pre>
 * <p>
 * Note that this factory can only inject <em>interfaces</em>, not concrete classes.
 */
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

    private Class<T> mapperInterface;

    // 是否将mapper接口添加到mybatis，默认true
    private boolean addToConfig = true;

    public MapperFactoryBean() {
        //intentionally empty
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    protected void checkDaoConfig() {
        super.checkDaoConfig();

        notNull(this.mapperInterface, "Property 'mapperInterface' is required");

        Configuration configuration = getSqlSession().getConfiguration();
        if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
            try {

                // ==== 注册Mapper接口
                configuration.addMapper(this.mapperInterface);
            } catch (Exception e) {
                logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
                throw new IllegalArgumentException(e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }

    /**
     * 获取Mapper接口的代理对象
     */
    @Override
    public T getObject() {
        return getSqlSession().getMapper(this.mapperInterface);
    }

    @Override
    public Class<T> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    //------------- mutators --------------

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    /**
     * If addToConfig is false, the mapper will not be added to MyBatis.
     * This means it must have been included in mybatis-config.xml.
     * If it is true, the mapper will be added to MyBatis in the case it is not already registered.
     * <p/>
     * By default addToCofig is true.
     */
    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    /**
     * Return the flag for addition into MyBatis config.
     *
     * @return true if the mapper will be added to MyBatis in the case it is not already registered.
     */
    public boolean isAddToConfig() {
        return addToConfig;
    }
}
