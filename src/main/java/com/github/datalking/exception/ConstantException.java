package com.github.datalking.exception;

/**
 * @author yaoo on 5/29/18
 */
public class ConstantException extends IllegalArgumentException {

    public ConstantException(String className, String field, String message) {
        super("Field '" + field + "' " + message + " in class [" + className + "]");
    }

    public ConstantException(String className, String namePrefix, Object value) {
        super("No '" + namePrefix + "' field with value '" + value + "' found in class [" + className + "]");
    }

}
