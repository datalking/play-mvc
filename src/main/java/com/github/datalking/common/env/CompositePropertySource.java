package com.github.datalking.common.env;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author yaoo on 5/29/18
 */
public class CompositePropertySource extends PropertySource<Object> {

    private final Set<PropertySource<?>> propertySources = new LinkedHashSet<>();

    public CompositePropertySource(String name) {
        super(name);
    }

    @Override
    public Object getProperty(String name) {
        for (PropertySource<?> propertySource : this.propertySources) {
            Object candidate = propertySource.getProperty(name);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    public void addPropertySource(PropertySource<?> propertySource) {
        this.propertySources.add(propertySource);
    }

    @Override
    public String toString() {
        return String.format("%s [name='%s', propertySources=%s]",
                getClass().getSimpleName(), this.name, this.propertySources);
    }

}
