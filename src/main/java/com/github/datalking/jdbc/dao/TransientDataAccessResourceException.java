package com.github.datalking.jdbc.dao;

/**
 * @author yaoo on 5/27/18
 */
public class TransientDataAccessResourceException extends TransientDataAccessException {

    /**
     * Constructor for TransientDataAccessResourceException.
     *
     * @param msg the detail message
     */
    public TransientDataAccessResourceException(String msg) {
        super(msg);
    }

    /**
     * Constructor for TransientDataAccessResourceException.
     *
     * @param msg   the detail message
     * @param cause the root cause from the data access API in use
     */
    public TransientDataAccessResourceException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
