package com.github.datalking.jdbc.transaction.exception;

/**
 * @author yaoo on 5/30/18
 */
public class IllegalTransactionStateException extends TransactionException {

    public IllegalTransactionStateException(String msg) {
        super(msg);
    }

    public IllegalTransactionStateException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
