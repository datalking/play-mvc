package com.github.datalking.jdbc.transaction;

/**
 * @author yaoo on 5/27/18
 */
public abstract class TransactionException extends RuntimeException {

    public TransactionException(String msg) {
        super(msg);
    }

    public TransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
