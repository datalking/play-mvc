package com.github.datalking.jdbc.support;

import com.github.datalking.jdbc.dao.DataAccessException;

import java.sql.SQLException;

/**
 * @author yaoo on 5/27/18
 */
public interface SQLExceptionTranslator {

    DataAccessException translate(String task, String sql, SQLException ex);

}
