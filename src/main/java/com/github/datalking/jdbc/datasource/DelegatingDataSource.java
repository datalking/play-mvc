package com.github.datalking.jdbc.datasource;

import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.util.Assert;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author yaoo on 5/27/18
 */
public class DelegatingDataSource implements DataSource, InitializingBean {

    private DataSource targetDataSource;

    public DelegatingDataSource() {
    }

    public DelegatingDataSource(DataSource targetDataSource) {
        setTargetDataSource(targetDataSource);
    }

    public void setTargetDataSource(DataSource targetDataSource) {
        Assert.notNull(targetDataSource, "'targetDataSource' must not be null");
        this.targetDataSource = targetDataSource;
    }

    public DataSource getTargetDataSource() {
        return this.targetDataSource;
    }

    @Override
    public void afterPropertiesSet() {
        if (getTargetDataSource() == null) {
            throw new IllegalArgumentException("Property 'targetDataSource' is required");
        }
    }

    public Connection getConnection() throws SQLException {
        return getTargetDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getTargetDataSource().getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return getTargetDataSource().getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        getTargetDataSource().setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return getTargetDataSource().getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        getTargetDataSource().setLoginTimeout(seconds);
    }


    //---------------------------------------------------------------------
    // Implementation of JDBC 4.0's Wrapper interface
    //---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return getTargetDataSource().unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || getTargetDataSource().isWrapperFor(iface));
    }


    //---------------------------------------------------------------------
    // Implementation of JDBC 4.1's getParentLogger method
    //---------------------------------------------------------------------

    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

}
