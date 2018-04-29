package com.github.datalking.web.http;

import java.net.URI;

/**
 * @author yaoo on 4/29/18
 */
public interface HttpRequest extends HttpMessage {

    HttpMethod getMethod();

    URI getURI();

}
