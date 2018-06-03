package com.github.datalking.common.env;

import java.util.Map;

/**
 * @author yaoo on 5/28/18
 */
public interface ConfigurableEnvironment extends Environment, ConfigurablePropertyResolver {

    MutablePropertySources getPropertySources();

    void setActiveProfiles(String... profiles);

    void addActiveProfile(String profile);

    void setDefaultProfiles(String... profiles);

    Map<String, Object> getSystemEnvironment();

    Map<String, Object> getSystemProperties();

    void merge(ConfigurableEnvironment parent);

}
