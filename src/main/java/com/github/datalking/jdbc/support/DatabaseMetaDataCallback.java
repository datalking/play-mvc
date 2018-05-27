package com.github.datalking.jdbc.support;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author yaoo on 5/27/18
 */
public interface DatabaseMetaDataCallback {

    Object processMetaData(DatabaseMetaData dbmd) throws SQLException, Exception;

}
