package com.github.datalking.web.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author yaoo on 4/26/18
 */
public interface HttpOutputMessage extends HttpMessage {

    OutputStream getBody() throws IOException;

}
