package com.github.datalking.common.meta;

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

    Map<String, Object> getAnnotationAttributes(String annotationType);

//    boolean isFinal();
//    boolean isStatic();
//    boolean isAbstract();

}
