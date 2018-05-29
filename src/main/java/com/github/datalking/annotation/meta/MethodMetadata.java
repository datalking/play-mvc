package com.github.datalking.annotation.meta;

import java.util.Map;

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

    Map<String, Object> getAnnotationAttributes(String annotationType);

}
