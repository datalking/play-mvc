package com.github.datalking.web.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author yaoo on 4/26/18
 */
public interface HttpInputMessage extends HttpMessage {

    InputStream getBody() throws IOException;

}
