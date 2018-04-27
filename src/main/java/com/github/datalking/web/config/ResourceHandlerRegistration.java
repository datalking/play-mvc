package com.github.datalking.web.config;

import com.github.datalking.io.Resource;
import com.github.datalking.io.ResourceLoader;
import com.github.datalking.util.Assert;
import com.github.datalking.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yaoo on 4/27/18
 */
public class ResourceHandlerRegistration {

    private final ResourceLoader resourceLoader;

    private final String[] pathPatterns;

    private final List<Resource> locations = new ArrayList<>();

    private Integer cachePeriod;

    public ResourceHandlerRegistration(ResourceLoader resourceLoader, String... pathPatterns) {
        Assert.notNull(pathPatterns, "At least one path pattern is required for resource handling.");
        this.resourceLoader = resourceLoader;
        this.pathPatterns = pathPatterns;
    }

    public ResourceHandlerRegistration addResourceLocations(String... resourceLocations) {
        for (String location : resourceLocations) {
            this.locations.add(resourceLoader.getResource(location));
        }
        return this;
    }

    public ResourceHandlerRegistration setCachePeriod(Integer cachePeriod) {
        this.cachePeriod = cachePeriod;
        return this;
    }


    protected String[] getPathPatterns() {
        return pathPatterns;
    }


//    protected ResourceHttpRequestHandler getRequestHandler() {
//        Assert.isTrue(!CollectionUtils.isEmpty(locations), "At least one location is required for resource handling.");
//        ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
//        requestHandler.setLocations(locations);
//        if (cachePeriod != null) {
//            requestHandler.setCacheSeconds(cachePeriod);
//        }
//        return requestHandler;
//    }

}
