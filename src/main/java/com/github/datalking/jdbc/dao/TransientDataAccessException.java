package com.github.datalking.jdbc.dao;

/**
 * @author yaoo on 5/27/18
 */
public abstract class TransientDataAccessException extends DataAccessException {

    /**
     * Constructor for TransientDataAccessException.
     *
     * @param msg the detail message
     */
    public TransientDataAccessException(String msg) {
        super(msg);
    }

    /**
     * Constructor for TransientDataAccessException.
     *
     * @param msg   the detail message
     * @param cause the root cause (usually from using a underlying data access API such as JDBC)
     */
    public TransientDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
