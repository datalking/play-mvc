package com.github.datalking.jdbc;

import com.github.datalking.jdbc.dao.DataAccessException;

import java.sql.SQLException;

/**
 * @author yaoo on 5/27/18
 */
public class BadSqlGrammarException extends DataAccessException {

    private String sql;


    /**
     * Constructor for BadSqlGrammarException.
     *
     * @param task name of current task
     * @param sql  the offending SQL statement
     * @param ex   the root cause
     */
    public BadSqlGrammarException(String task, String sql, SQLException ex) {
        super(task + "; bad SQL grammar [" + sql + "]", ex);
        this.sql = sql;
    }


    /**
     * Return the wrapped SQLException.
     */
    public SQLException getSQLException() {
        return (SQLException) getCause();
    }

    /**
     * Return the SQL that caused the problem.
     */
    public String getSql() {
        return this.sql;
    }

}
