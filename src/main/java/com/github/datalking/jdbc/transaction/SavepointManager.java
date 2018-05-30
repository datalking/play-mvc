package com.github.datalking.jdbc.transaction;

import com.github.datalking.jdbc.transaction.exception.TransactionException;

/**
 * @author yaoo on 5/30/18
 */
public interface SavepointManager {

    Object createSavepoint() throws TransactionException;

    void rollbackToSavepoint(Object savepoint) throws TransactionException;

    void releaseSavepoint(Object savepoint) throws TransactionException;

}
