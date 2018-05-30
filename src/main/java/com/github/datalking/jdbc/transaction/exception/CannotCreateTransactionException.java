package com.github.datalking.jdbc.transaction.exception;

/**
 * @author yaoo on 5/30/18
 */
public class CannotCreateTransactionException extends TransactionException {

    public CannotCreateTransactionException(String msg) {
        super(msg);
    }

    public CannotCreateTransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
