package com.github.datalking.annotation.meta;

/**
 * 获取java方法元数据 接口
 *
 * @author yaoo on 4/13/18
 */
public interface MethodMetadata {

    String getMethodName();

    String getDeclaringClassName();

    String getReturnTypeName();

//    boolean isFinal();
//    boolean isStatic();
//    boolean isAbstract();

}
