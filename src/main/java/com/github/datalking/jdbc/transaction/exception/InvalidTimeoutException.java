package com.github.datalking.jdbc.transaction.exception;

/**
 * @author yaoo on 5/30/18
 */
public class InvalidTimeoutException extends TransactionException {

    private int timeout;

    public InvalidTimeoutException(String msg, int timeout) {
        super(msg);
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

}
