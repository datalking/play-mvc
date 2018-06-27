package com.github.datalking;

import com.github.datalking.jdbc.dao.DataAccessException;
import com.github.datalking.jdbc.dao.PersistenceExceptionTranslator;
import com.github.datalking.jdbc.dao.TransientDataAccessResourceException;
import com.github.datalking.jdbc.datasource.DataSourceUtils;
import com.github.datalking.jdbc.transaction.TransactionSynchronizationAdapter;
import com.github.datalking.jdbc.transaction.TransactionSynchronizationManager;
import com.github.datalking.transaction.SpringManagedTransactionFactory;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import static com.github.datalking.util.Assert.notNull;

/**
 * SqlSession操作 工具类
 * Handles MyBatis SqlSession life cycle. It can register and get SqlSessions from
 * Spring {@code TransactionSynchronizationManager}. Also works if no transaction is active.
 */
public abstract class SqlSessionUtils {

    private static final Log LOGGER = LogFactory.getLog(SqlSessionUtils.class);

    private static final String NO_EXECUTOR_TYPE_SPECIFIED = "No ExecutorType specified";
    private static final String NO_SQL_SESSION_FACTORY_SPECIFIED = "No SqlSessionFactory specified";
    private static final String NO_SQL_SESSION_SPECIFIED = "No SqlSession specified";

    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory) {

        ExecutorType executorType = sessionFactory.getConfiguration().getDefaultExecutorType();

        return getSqlSession(sessionFactory, executorType, null);
    }

    /**
     * Gets an SqlSession from Spring Transaction Manager or creates a new one if needed.
     * Tries to get a SqlSession out of current transaction. If there is not any, it creates a new one.
     * Then, it synchronizes the SqlSession with the transaction if Spring TX is active and
     * <code>SpringManagedTransactionFactory</code> is configured as a transaction manager.
     *
     * @param sessionFactory      a MyBatis {@code SqlSessionFactory} to create new sessions
     * @param executorType        The executor type of the SqlSession to create
     * @param exceptionTranslator Optional. Translates SqlSession.commit() exceptions to Spring exceptions.
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory,
                                           ExecutorType executorType,
                                           PersistenceExceptionTranslator exceptionTranslator) {

        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
        notNull(executorType, NO_EXECUTOR_TYPE_SPECIFIED);

        // 从Spring事务管理器中获取SqlSessionHolder，其中封装了SqlSession对象，一般得到null
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        SqlSession session = sessionHolder(executorType, holder);
        if (session != null) {
            return session;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating a new SqlSession");
        }

        // 若上述SqlSession为空，则通过SqlSessionFactory创建新的SqlSession对象
        session = sessionFactory.openSession(executorType);

        // 将SqlSession对象与Spring事务管理器绑定
        registerSessionHolder(sessionFactory, executorType, exceptionTranslator, session);

        return session;
    }

    /**
     * Register session holder if synchronization is active (i.e. a Spring TX is active).
     * <p>
     * Note: The DataSource used by the Environment should be synchronized with the
     * transaction either through DataSourceTxMgr or another tx synchronization.
     * Further assume that if an exception is thrown, whatever started the transaction will
     * handle closing / rolling back the Connection associated with the SqlSession.
     *
     * @param sessionFactory      sqlSessionFactory used for registration.
     * @param executorType        executorType used for registration.
     * @param exceptionTranslator persistenceExceptionTranslater used for registration.
     * @param session             sqlSession used for registration.
     */
    private static void registerSessionHolder(SqlSessionFactory sessionFactory,
                                              ExecutorType executorType,
                                              PersistenceExceptionTranslator exceptionTranslator,
                                              SqlSession session) {

        SqlSessionHolder holder;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Environment environment = sessionFactory.getConfiguration().getEnvironment();

            if (environment.getTransactionFactory() instanceof SpringManagedTransactionFactory) {

                holder = new SqlSessionHolder(session, executorType, exceptionTranslator);
                TransactionSynchronizationManager.bindResource(sessionFactory, holder);
                TransactionSynchronizationManager.registerSynchronization(new SqlSessionSynchronization(holder, sessionFactory));
                holder.setSynchronizedWithTransaction(true);
                holder.requested();
            } else {
                if (TransactionSynchronizationManager.getResource(environment.getDataSource()) == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("SqlSession [" + session + "] was not registered for synchronization because DataSource is not transactional");
                    }
                } else {
                    throw new TransientDataAccessResourceException("SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use Spring transaction synchronization");
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SqlSession [" + session + "] was not registered for synchronization because synchronization is not active");
            }
        }
    }

    private static SqlSession sessionHolder(ExecutorType executorType, SqlSessionHolder holder) {
        SqlSession session = null;
        if (holder != null && holder.isSynchronizedWithTransaction()) {
            if (holder.getExecutorType() != executorType) {
                throw new TransientDataAccessResourceException("Cannot change the ExecutorType when there is an existing transaction");
            }

            holder.requested();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fetched SqlSession [" + holder.getSqlSession() + "] from current transaction");
            }

            session = holder.getSqlSession();
        }
        return session;
    }

    /**
     * Checks if {@code SqlSession} passed as an argument is managed by Spring {@code TransactionSynchronizationManager}
     * If it is not, it closes it, otherwise it just updates the reference counter and
     * lets Spring call the close callback when the managed transaction ends
     */
    public static void closeSqlSession(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        if ((holder != null) && (holder.getSqlSession() == session)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Releasing transactional SqlSession [" + session + "]");
            }
            holder.released();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Closing non transactional SqlSession [" + session + "]");
            }
            session.close();
        }
    }

    /**
     * Returns if the {@code SqlSession} passed as an argument is being managed by Spring
     *
     * @param session        a MyBatis SqlSession to check
     * @param sessionFactory the SqlSessionFactory which the SqlSession was built with
     * @return true if session is transactional, otherwise false
     */
    public static boolean isSqlSessionTransactional(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        return (holder != null) && (holder.getSqlSession() == session);
    }

    /**
     * Callback for cleaning up resources. It cleans TransactionSynchronizationManager and
     * also commits and closes the {@code SqlSession}.
     * It assumes that {@code Connection} life cycle will be managed by
     * {@code DataSourceTransactionManager} or {@code JtaTransactionManager}
     */
    private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter {

        private final SqlSessionHolder holder;

        private final SqlSessionFactory sessionFactory;

        private boolean holderActive = true;

        public SqlSessionSynchronization(SqlSessionHolder holder, SqlSessionFactory sessionFactory) {
            notNull(holder, "Parameter 'holder' must be not null");
            notNull(sessionFactory, "Parameter 'sessionFactory' must be not null");

            this.holder = holder;
            this.sessionFactory = sessionFactory;
        }

        @Override
        public int getOrder() {
            // order right before any Connection synchronization
            return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 1;
        }

        @Override
        public void suspend() {
            if (this.holderActive) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization suspending SqlSession [" + this.holder.getSqlSession() + "]");
                }
                TransactionSynchronizationManager.unbindResource(this.sessionFactory);
            }
        }

        @Override
        public void resume() {
            if (this.holderActive) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization resuming SqlSession [" + this.holder.getSqlSession() + "]");
                }
                TransactionSynchronizationManager.bindResource(this.sessionFactory, this.holder);
            }
        }


        @Override
        public void beforeCommit(boolean readOnly) {
            // Connection commit or rollback will be handled by ConnectionSynchronization or
            // DataSourceTransactionManager.
            // But, do cleanup the SqlSession / Executor, including flushing BATCH statements so
            // they are actually executed.
            // SpringManagedTransaction will no-op the commit over the jdbc connection
            // TODO This updates 2nd level caches but the tx may be rolledback later on!
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Transaction synchronization committing SqlSession [" + this.holder.getSqlSession() + "]");
                    }
                    this.holder.getSqlSession().commit();
                } catch (PersistenceException p) {
                    if (this.holder.getPersistenceExceptionTranslator() != null) {
                        DataAccessException translated = this.holder
                                .getPersistenceExceptionTranslator()
                                .translateExceptionIfPossible(p);
                        if (translated != null) {
                            throw translated;
                        }
                    }
                    throw p;
                }
            }
        }

        @Override
        public void beforeCompletion() {
            // Issue #18 Close SqlSession and deregister it now
            // because afterCompletion may be called from a different thread
            if (!this.holder.isOpen()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization deregistering SqlSession [" + this.holder.getSqlSession() + "]");
                }
                TransactionSynchronizationManager.unbindResource(sessionFactory);
                this.holderActive = false;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]");
                }
                this.holder.getSqlSession().close();
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (this.holderActive) {
                // afterCompletion may have been called from a different thread
                // so avoid failing if there is nothing in this one
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization deregistering SqlSession [" + this.holder.getSqlSession() + "]");
                }
                TransactionSynchronizationManager.unbindResourceIfPossible(sessionFactory);
                this.holderActive = false;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]");
                }
                this.holder.getSqlSession().close();
            }
            this.holder.reset();
        }
    }

}
