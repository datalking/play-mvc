package com.github.datalking.io;

/**
 * @author yaoo on 5/28/18
 */
public interface ResourcePatternResolver extends ResourceLoader {

    String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    Resource[] getResources(String locationPattern);

}
