package com.github.datalking.jdbc.transaction.exception;

/**
 * @author yaoo on 5/30/18
 */
public class UnexpectedRollbackException extends TransactionException {

    public UnexpectedRollbackException(String msg) {
        super(msg);
    }

    public UnexpectedRollbackException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
