package com.github.datalking.jdbc.datasource;

import java.sql.Connection;

/**
 * @author yaoo on 5/27/18
 */
public interface ConnectionProxy extends Connection {

    Connection getTargetConnection();

}
