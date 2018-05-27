package com.github.datalking.jdbc.transaction;

/**
 * @author yaoo on 5/27/18
 */
public interface ResourceTransactionManager extends PlatformTransactionManager {

    Object getResourceFactory();

}
