package com.github.datalking.io;

import com.github.datalking.common.env.PropertiesPropertySource;
import com.github.datalking.util.StringUtils;

/**
 * 读取指定位置的属性文件，创建ResourcePropertySource
 *
 * @author yaoo on 5/28/18
 */
public class ResourcePropertySource extends PropertiesPropertySource {

    public ResourcePropertySource(String name, EncodedResource resource) {
        // 先加载资源，再创建ResourcePropertySource
        super(name, PropertiesLoaderUtils.loadProperties(resource));
    }

    public ResourcePropertySource(EncodedResource resource) {
        // 先加载资源，再创建ResourcePropertySource，默认名为resource class名@hashcode
        this(getNameForResource(resource.getResource()), resource);
    }

    public ResourcePropertySource(String name, Resource resource) {
        super(name, PropertiesLoaderUtils.loadProperties(new EncodedResource(resource)));
    }

    public ResourcePropertySource(Resource resource) {
        this(getNameForResource(resource), resource);
    }

    public ResourcePropertySource(String name, String location, ClassLoader classLoader) {
        this(name, new DefaultResourceLoader(classLoader).getResource(location));
    }

    public ResourcePropertySource(String location, ClassLoader classLoader) {
        // 先加载资源，再创建ResourcePropertySource，默认名为resource class名@hashcode
        this(new DefaultResourceLoader(classLoader).getResource(location));
    }

    public ResourcePropertySource(String name, String location) {
        this(name, new DefaultResourceLoader().getResource(location));
    }

    public ResourcePropertySource(String location) {
        this(new DefaultResourceLoader().getResource(location));
    }


    /**
     * Return the description string for the resource, and if empty returns
     * the class name of the resource plus its identity hash code.
     */
    private static String getNameForResource(Resource resource) {
        String name = resource.getDescription();
        if (!StringUtils.hasText(name)) {
            name = resource.getClass().getSimpleName() + "@" + System.identityHashCode(resource);
        }
        return name;
    }

}
