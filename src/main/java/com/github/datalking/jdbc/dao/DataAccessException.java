package com.github.datalking.jdbc.dao;

/**
 * @author yaoo on 5/26/18
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
