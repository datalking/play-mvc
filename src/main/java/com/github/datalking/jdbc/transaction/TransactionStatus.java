package com.github.datalking.jdbc.transaction;

/**
 * @author yaoo on 5/27/18
 */
public interface TransactionStatus {

    boolean isNewTransaction();

    boolean hasSavepoint();

    void setRollbackOnly();

    void flush();

    boolean isCompleted();

}
