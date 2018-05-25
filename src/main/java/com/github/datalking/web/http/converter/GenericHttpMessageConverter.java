package com.github.datalking.web.http.converter;

import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 读取并转换http请求信息的具体接口
 *
 * @author yaoo on 4/29/18
 */
public interface GenericHttpMessageConverter<T> extends HttpMessageConverter<T> {

    boolean canRead(Type type, Class<?> contextClass, MediaType mediaType);

    T read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException;

}
