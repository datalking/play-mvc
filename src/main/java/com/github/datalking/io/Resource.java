package com.github.datalking.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource代表统一资源 接口
 */
public interface Resource {

    boolean exists();

    InputStream getInputStream() throws IOException;

    String getFilename();

}
