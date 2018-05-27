package com.github.datalking.jdbc.dao;

/**
 * @author yaoo on 5/27/18
 */
public interface PersistenceExceptionTranslator {

    DataAccessException translateExceptionIfPossible(RuntimeException ex);

}
