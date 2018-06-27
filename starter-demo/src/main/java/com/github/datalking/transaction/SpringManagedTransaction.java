package com.github.datalking.transaction;

import com.github.datalking.jdbc.datasource.ConnectionHolder;
import com.github.datalking.jdbc.datasource.DataSourceUtils;
import com.github.datalking.jdbc.transaction.TransactionSynchronizationManager;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.github.datalking.util.Assert.notNull;

/**
 * spring事务管理器
 * <p>
 * SpringManagedTransactio} handles the lifecycle of a JDBC connection.
 * It retrieves a connection from Spring's transaction manager and returns it back to it when no longer needed.
 * <p>
 * If Spring's transaction handling is active, it will no-op all commit/rollback/close calls
 * assuming that the Spring transaction manager will do the job.
 * <p>
 * If it is not, it will behave like {@code JdbcTransaction}.
 *
 */
public class SpringManagedTransaction implements Transaction {

    private static final Log LOGGER = LogFactory.getLog(SpringManagedTransaction.class);

    // 与当前数据库连接对象关联的数据源对象
    private final DataSource dataSource;

    // 当前事务管理中维护的数据库连接对象
    private Connection connection;

    // 标识该数据库连接对象是否由Spring的事务管理器管理
    private boolean isConnectionTransactional;

    // 事务是否自动提交
    private boolean autoCommit;

    public SpringManagedTransaction(DataSource dataSource) {
        notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {

        if (this.connection == null) {
            openConnection();
        }

        return this.connection;
    }

    /**
     * Gets a connection from Spring transaction manager and discovers if this
     * {@code Transaction} should manage connection or let it to Spring.
     * <p>
     * It also reads autocommit setting because when using Spring Transaction MyBatis
     * thinks that autocommit is always false and will always call commit/rollback
     * so we need to no-op that calls.
     */
    private void openConnection() throws SQLException {

        // 从Spring事务管理器中获取数据库连接对象，首先尝试从事务上下文中获取数据库连接，若获取成功则返回，否则从数据源获取数据库连接并返回
        try {
            this.connection = DataSourceUtils.getConnection(this.dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.autoCommit = this.connection.getAutoCommit();
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "JDBC Connection ["
                            + this.connection
                            + "] will"
                            + (this.isConnectionTransactional ? " " : " not ")
                            + "be managed by Spring");
        }
    }

    @Override
    public void commit() throws SQLException {

        /// 若事务不由Spring事务管理器管理，且不需要自动提交，则在此处真正提交事务
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            this.connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {

        /// 若事务不由Spring事务管理器管理，且不需要自动提交，则在此处真正回滚事务
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            this.connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {

        // 将数据库连接归还给Spring事务管理器
        DataSourceUtils.releaseConnection(this.connection, this.dataSource);
    }

    @Override
    public Integer getTimeout() throws SQLException {
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (holder != null && holder.hasTimeout()) {
            return holder.getTimeToLiveInSeconds();
        }
        return null;
    }

}
