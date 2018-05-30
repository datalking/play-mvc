package com.github.datalking;

import com.github.datalking.jdbc.UncategorizedDataAccessException;

/**
 * MyBatis specific subclass of UncategorizedDataAccessException,
 * for MyBatis system errors that do not match any concrete dao exceptions.
 * <p>
 * In MyBatis 3 {@code org.apache.ibatis.exceptions.PersistenceException} is a {@code RuntimeException},
 * but using this wrapper class to bring everything under a single hierarchy will be easier for client code.
 */
public class MyBatisSystemException extends UncategorizedDataAccessException {

    private static final long serialVersionUID = -5284728621670758939L;

    public MyBatisSystemException(Throwable cause) {
        super(null, cause);
    }

}
