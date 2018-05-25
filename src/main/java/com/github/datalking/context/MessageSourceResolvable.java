package com.github.datalking.context;

/**
 * @author yaoo on 5/7/18
 */
public interface MessageSourceResolvable {

    String[] getCodes();

    Object[] getArguments();

    String getDefaultMessage();

}
