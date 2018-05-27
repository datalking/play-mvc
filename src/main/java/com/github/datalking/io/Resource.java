package com.github.datalking.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource是统一定位资源的接口
 */
public interface Resource {

    boolean exists();

    InputStream getInputStream() throws IOException;


}
