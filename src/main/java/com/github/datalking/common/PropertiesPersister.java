package com.github.datalking.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/**
 * @author yaoo on 5/28/18
 */
public interface PropertiesPersister {

    void load(Properties props, InputStream is) throws IOException;

    void load(Properties props, Reader reader) throws IOException;

    void store(Properties props, OutputStream os, String header) throws IOException;

    void store(Properties props, Writer writer, String header) throws IOException;

    void loadFromXml(Properties props, InputStream is) throws IOException;

    void storeToXml(Properties props, OutputStream os, String header) throws IOException;

    void storeToXml(Properties props, OutputStream os, String header, String encoding) throws IOException;

}
