package com.github.datalking.jdbc.datasource;

import java.sql.Connection;

/**
 * @author yaoo on 5/27/18
 */
public interface ConnectionHandle {

    Connection getConnection();

    void releaseConnection(Connection con);

}
