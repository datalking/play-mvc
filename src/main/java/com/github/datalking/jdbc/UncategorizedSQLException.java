package com.github.datalking.jdbc;

import com.github.datalking.jdbc.dao.DataAccessException;

import java.sql.SQLException;

/**
 * @author yaoo on 5/27/18
 */
public class UncategorizedSQLException extends DataAccessException {

    private final String sql;


    /**
     * Constructor for UncategorizedSQLException.
     *
     * @param task name of current task
     * @param sql  the offending SQL statement
     * @param ex   the root cause
     */
    public UncategorizedSQLException(String task, String sql, SQLException ex) {
        super(task + "; uncategorized SQLException for SQL [" + sql + "]; SQL state [" +
                ex.getSQLState() + "]; error code [" + ex.getErrorCode() + "]; " + ex.getMessage(), ex);
        this.sql = sql;
    }

    /**
     * Return the underlying SQLException.
     */
    public SQLException getSQLException() {
        return (SQLException) getCause();
    }

    /**
     * Return the SQL that led to the problem.
     */
    public String getSql() {
        return this.sql;
    }
}
