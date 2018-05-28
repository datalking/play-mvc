package com.github.datalking.common.env;

/**
 * PropertySource的容器
 *
 * @author yaoo on 5/28/18
 */
public interface PropertySources extends Iterable<PropertySource<?>> {

    boolean contains(String name);

    PropertySource<?> get(String name);

}
