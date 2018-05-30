package com.github.datalking;

import com.github.datalking.jdbc.dao.DataAccessException;
import com.github.datalking.jdbc.dao.PersistenceExceptionTranslator;
import com.github.datalking.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import com.github.datalking.jdbc.support.SQLExceptionTranslator;
import com.github.datalking.jdbc.transaction.exception.TransactionException;
import org.apache.ibatis.exceptions.PersistenceException;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 异常转换器
 * 将mybatis的异常转换成spring的异常
 * Default exception translator.
 * <p>
 * Translates MyBatis SqlSession returned exception into a Spring {@code DataAccessException} using Spring's {@code SQLExceptionTranslator}
 * Can load {@code SQLExceptionTranslator} eagerly of when the first exception is translated.
 */
public class MyBatisExceptionTranslator implements PersistenceExceptionTranslator {

    private final DataSource dataSource;

    private SQLExceptionTranslator exceptionTranslator;

    /**
     * Creates a new {@code DataAccessExceptionTranslator} instance.
     *
     * @param dataSource                  DataSource to use to find metadata and establish which error codes are usable.
     * @param exceptionTranslatorLazyInit if true, the translator instantiates internal stuff only the first time will
     *                                    have the need to translate exceptions.
     */
    public MyBatisExceptionTranslator(DataSource dataSource, boolean exceptionTranslatorLazyInit) {
        this.dataSource = dataSource;

        if (!exceptionTranslatorLazyInit) {
            this.initExceptionTranslator();
        }
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException e) {
        if (e instanceof PersistenceException) {
            if (e.getCause() instanceof PersistenceException) {
                e = (PersistenceException) e.getCause();
            }
            if (e.getCause() instanceof SQLException) {
                this.initExceptionTranslator();
                return this.exceptionTranslator.translate(e.getMessage() + "\n", null, (SQLException) e.getCause());
            } else if (e.getCause() instanceof TransactionException) {
                throw (TransactionException) e.getCause();
            }
            return new MyBatisSystemException(e);
        }
        return null;
    }

    private synchronized void initExceptionTranslator() {
        if (this.exceptionTranslator == null) {
            this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
        }
    }

}
