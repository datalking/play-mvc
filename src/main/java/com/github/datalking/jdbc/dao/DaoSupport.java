package com.github.datalking.jdbc.dao;

import com.github.datalking.beans.factory.InitializingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic base class for DAOs, defining template methods for DAO initialization.
 * <p>
 * Extended by Spring's specific DAO support classes, such as: JdbcDaoSupport, JdoDaoSupport, etc.
 */
public abstract class DaoSupport implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final void afterPropertiesSet() {

        // Let abstract subclasses check their configuration.
        checkDaoConfig();

        // Let concrete implementations initialize themselves.
        try {

            // 默认为空方法
            initDao();
        } catch (Exception ex) {
//            throw new BeanInitializationException("Initialization of DAO failed", ex);
            ex.printStackTrace();
        }
    }

    protected abstract void checkDaoConfig() throws IllegalArgumentException;

    protected void initDao() throws Exception {
    }

}
