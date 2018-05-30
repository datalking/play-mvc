package com.github.datalking.jdbc.support;

/**
 * @author yaoo on 5/30/18
 */
public interface SmartTransactionObject {

    boolean isRollbackOnly();

    void flush();

}
