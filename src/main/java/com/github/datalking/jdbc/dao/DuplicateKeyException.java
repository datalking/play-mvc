package com.github.datalking.jdbc.dao;

/**
 * @author yaoo on 5/27/18
 */
public class DuplicateKeyException extends DataAccessException {

    /**
     * Constructor for DuplicateKeyException.
     * @param msg the detail message
     */
    public DuplicateKeyException(String msg) {
        super(msg);
    }

    /**
     * Constructor for DuplicateKeyException.
     * @param msg the detail message
     * @param cause the root cause from the data access API in use
     */
    public DuplicateKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
