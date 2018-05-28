package com.github.datalking.io;

import com.github.datalking.common.env.PropertiesPropertySource;
import com.github.datalking.util.StringUtils;

import java.io.IOException;

/**
 * @author yaoo on 5/28/18
 */
public class ResourcePropertySource extends PropertiesPropertySource {

    public ResourcePropertySource(String name, EncodedResource resource)  {
        super(name, PropertiesLoaderUtils.loadProperties(resource));
    }

    public ResourcePropertySource(EncodedResource resource)  {
        this(getNameForResource(resource.getResource()), resource);
    }

    public ResourcePropertySource(String name, Resource resource)  {
        super(name, PropertiesLoaderUtils.loadProperties(new EncodedResource(resource)));
    }

    public ResourcePropertySource(Resource resource)  {
        this(getNameForResource(resource), resource);
    }

    public ResourcePropertySource(String name, String location, ClassLoader classLoader)  {
//        this(name, new DefaultResourceLoader(classLoader).getResource(location));
        this(name, new DefaultResourceLoader().getResource(location));
    }

    public ResourcePropertySource(String location, ClassLoader classLoader)  {
//        this(new DefaultResourceLoader(classLoader).getResource(location));
        this(new DefaultResourceLoader().getResource(location));
    }

    public ResourcePropertySource(String name, String location)  {
        this(name, new DefaultResourceLoader().getResource(location));
    }

    public ResourcePropertySource(String location)  {
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
