package com.github.datalking.common.convert;

/**
 * @author yaoo on 5/9/18
 */
public class SimpleTypeConverter extends TypeConverterSupport {

    public SimpleTypeConverter() {
        this.typeConverterDelegate = new TypeConverterDelegate(this);
        registerDefaultEditors();
    }

}
