package com.github.datalking.jdbc.dao;

import com.github.datalking.beans.factory.InitializingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic base class for DAOs, defining template methods for DAO initialization.
 *
 * <p>Extended by Spring's specific DAO support classes, such as: JdbcDaoSupport, JdoDaoSupport, etc.
 */
public abstract class DaoSupport implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final void afterPropertiesSet() {
        // Let abstract subclasses check their configuration.
        checkDaoConfig();

        // Let concrete implementations initialize themselves.
        try {
            initDao();
        } catch (Exception ex) {
//            throw new BeanInitializationException("Initialization of DAO failed", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Abstract subclasses must override this to check their configuration.
     * <p>Implementors should be marked as {@code final} if concrete subclasses
     * are not supposed to override this template method themselves.
     *
     * @throws IllegalArgumentException in case of illegal configuration
     */
    protected abstract void checkDaoConfig() throws IllegalArgumentException;

    /**
     * Concrete subclasses can override this for custom initialization behavior.
     * Gets called after population of this instance's bean properties.
     *
     * @throws Exception if DAO initialization fails (will be rethrown as a BeanInitializationException)
     */
    protected void initDao() throws Exception {
    }

}
