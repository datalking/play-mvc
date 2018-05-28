package com.github.datalking.io;

import com.github.datalking.util.ResourceUtils;

/**
 * 资源加载类
 */
public interface ResourceLoader {

    // Pseudo URL prefix for loading from the class path: "classpath:"
    String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

    /**
     * 加载资源
     *
     * @param location 资源路径
     * @return 加载完资源的Resource对象
     */
    Resource getResource(String location);
}

