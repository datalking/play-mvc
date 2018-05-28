package com.github.datalking.common.env;

/**
 * 代表应用程序运行的环境 接口
 * 包括profiles和properties
 *
 * @author yaoo on 5/28/18
 */
public interface Environment extends PropertyResolver {

    String[] getDefaultProfiles();

    String[] getActiveProfiles();

    boolean acceptsProfiles(String... profiles);

}
