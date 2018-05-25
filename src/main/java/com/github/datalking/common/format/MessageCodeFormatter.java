package com.github.datalking.common.format;

/**
 * @author yaoo on 5/10/18
 */
public interface MessageCodeFormatter {

    String format(String errorCode, String objectName, String field);

}
