package com.github.datalking.web.http.converter;

import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.HttpOutputMessage;
import com.github.datalking.web.http.MediaType;

import java.io.IOException;
import java.util.List;

/**
 * 消息转换最顶层接口
 * 负责将请求信息转换为一个对象（类型为T），将对象（类型为T）输出为响应信息
 *
 * @author yaoo on 4/26/18
 */
public interface HttpMessageConverter<T> {

    boolean canRead(Class<?> clazz, MediaType mediaType);

    boolean canWrite(Class<?> clazz, MediaType mediaType);

    T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException;

    void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException;

    List<MediaType> getSupportedMediaTypes();

}
