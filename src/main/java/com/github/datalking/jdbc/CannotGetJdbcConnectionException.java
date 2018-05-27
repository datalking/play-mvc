package com.github.datalking.jdbc;

import com.github.datalking.jdbc.dao.DataAccessException;

import java.sql.SQLException;

/**
 * @author yaoo on 5/27/18
 */
public class CannotGetJdbcConnectionException extends DataAccessException {

    public CannotGetJdbcConnectionException(String msg, SQLException ex) {
        super(msg, ex);
    }

}
