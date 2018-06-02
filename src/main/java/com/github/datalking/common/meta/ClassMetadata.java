package com.github.datalking.common.meta;

/**
 * @author yaoo on 4/13/18
 */
public interface ClassMetadata {

    String getClassName();

    boolean isInterface();

    boolean isAnnotation();

//    boolean isConcrete();
//    boolean isAbstract();
//    boolean isFinal();
//    boolean hasSuperClass();
//    String[] getInterfaceNames();

}
