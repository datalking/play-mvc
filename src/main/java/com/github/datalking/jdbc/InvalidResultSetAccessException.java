package com.github.datalking.jdbc;

import com.github.datalking.jdbc.dao.DataAccessException;

import java.sql.SQLException;

/**
 * @author yaoo on 5/27/18
 */
public class InvalidResultSetAccessException extends DataAccessException {

    private String sql;


    /**
     * Constructor for InvalidResultSetAccessException.
     *
     * @param task name of current task
     * @param sql  the offending SQL statement
     * @param ex   the root cause
     */
    public InvalidResultSetAccessException(String task, String sql, SQLException ex) {
        super(task + "; invalid ResultSet access for SQL [" + sql + "]", ex);
        this.sql = sql;
    }

    /**
     * Constructor for InvalidResultSetAccessException.
     *
     * @param ex the root cause
     */
    public InvalidResultSetAccessException(SQLException ex) {
        super(ex.getMessage(), ex);
    }

    /**
     * Return the wrapped SQLException.
     */
    public SQLException getSQLException() {
        return (SQLException) getCause();
    }

    /**
     * Return the SQL that caused the problem.
     *
     * @return the offending SQL, if known
     */
    public String getSql() {
        return this.sql;
    }

}
