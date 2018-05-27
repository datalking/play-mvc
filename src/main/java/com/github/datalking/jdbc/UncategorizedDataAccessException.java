package com.github.datalking.jdbc;

import com.github.datalking.jdbc.dao.DataAccessException;

/**
 * @author yaoo on 5/26/18
 */
public class UncategorizedDataAccessException extends DataAccessException {

    public UncategorizedDataAccessException(String message) {
        super(message);
    }

    public UncategorizedDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
