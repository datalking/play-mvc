package com.github.datalking.jdbc.datasource;

import com.github.datalking.util.Assert;

import java.sql.Connection;

/**
 * @author yaoo on 5/27/18
 */
public class SimpleConnectionHandle implements ConnectionHandle {

    private final Connection connection;

    public SimpleConnectionHandle(Connection connection) {
        Assert.notNull(connection, "Connection must not be null");
        this.connection = connection;
    }

    /**
     * Return the specified Connection as-is.
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * This implementation is empty, as we're using a standard
     * Connection handle that does not have to be released.
     */
    public void releaseConnection(Connection con) {
    }


    @Override
    public String toString() {
        return "SimpleConnectionHandle: " + this.connection;
    }

}
