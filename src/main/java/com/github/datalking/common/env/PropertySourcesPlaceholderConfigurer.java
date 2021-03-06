package com.github.datalking.common.env;

import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.beans.factory.config.PlaceholderConfigurerSupport;
import com.github.datalking.common.StringValueResolver;
import com.github.datalking.context.EnvironmentAware;
import com.github.datalking.exception.BeanInitializationException;

import java.io.IOException;
import java.util.Properties;

/**
 * properties配置文件属性占位符解析
 *
 * @author yaoo on 5/29/18
 */
public class PropertySourcesPlaceholderConfigurer extends PlaceholderConfigurerSupport
        implements EnvironmentAware {

    public static final String LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties";

    public static final String ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties";

    private MutablePropertySources propertySources;

    private Environment environment;

    public void setPropertySources(PropertySources propertySources) {
        this.propertySources = new MutablePropertySources(propertySources);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 替换 ${} 属性占位符的入口方法
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

        if (this.propertySources == null) {
            this.propertySources = new MutablePropertySources();

            if (this.environment != null) {
                this.propertySources.addLast(
                        new PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, this.environment) {
                            @Override
                            public String getProperty(String key) {
                                return this.source.getProperty(key);
                            }
                        }
                );
            }

            try {
                PropertySource<?> localPropertySource =
                        new PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, mergeProperties());

                if (this.localOverride) {
                    this.propertySources.addFirst(localPropertySource);
                } else {
                    this.propertySources.addLast(localPropertySource);
                }

            } catch (IOException ex) {
                throw new BeanInitializationException("Could not load properties", ex);
            }
        }

        processProperties(beanFactory, new PropertySourcesPropertyResolver(this.propertySources));
    }

    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     final ConfigurablePropertyResolver propertyResolver) {

        propertyResolver.setPlaceholderPrefix(this.placeholderPrefix);
        propertyResolver.setPlaceholderSuffix(this.placeholderSuffix);
        propertyResolver.setValueSeparator(this.valueSeparator);

        StringValueResolver valueResolver = new StringValueResolver() {

            @Override
            public String resolveStringValue(String strVal) {

                /// 第一次执行false，后面执行true，解析@Value注解中的占位符
                String resolved = ignoreUnresolvablePlaceholders ?
                        propertyResolver.resolvePlaceholders(strVal) :
                        propertyResolver.resolveRequiredPlaceholders(strVal);

                return (resolved.equals(nullValue) ? null : resolved);
            }
        };
        // 解析BeanDefinitionMap中所有的属性占位符
        doProcessProperties(beanFactoryToProcess, valueResolver);
    }

    @Override
    @Deprecated
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) {
        throw new UnsupportedOperationException("Call processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver) instead");
    }

}
