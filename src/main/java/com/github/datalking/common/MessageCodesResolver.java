package com.github.datalking.common;

/**
 * @author yaoo on 5/10/18
 */
public interface MessageCodesResolver {

    String[] resolveMessageCodes(String errorCode, String objectName);

    String[] resolveMessageCodes(String errorCode, String objectName, String field, Class<?> fieldType);

}
