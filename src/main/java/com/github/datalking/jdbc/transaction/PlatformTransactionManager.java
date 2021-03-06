package com.github.datalking.jdbc.transaction;

import com.github.datalking.jdbc.transaction.exception.TransactionException;

/**
 * @author yaoo on 5/27/18
 */
public interface PlatformTransactionManager {

    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;

}
