package com.github.datalking.jdbc.transaction.exception;

/**
 * @author yaoo on 5/30/18
 */
public class NestedTransactionNotSupportedException extends CannotCreateTransactionException {

    public NestedTransactionNotSupportedException(String msg) {
        super(msg);
    }

    public NestedTransactionNotSupportedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
