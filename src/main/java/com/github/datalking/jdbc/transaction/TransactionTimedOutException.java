package com.github.datalking.jdbc.transaction;

/**
 * @author yaoo on 5/27/18
 */
public class TransactionTimedOutException extends TransactionException {

    public TransactionTimedOutException(String msg) {
        super(msg);
    }

    public TransactionTimedOutException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
