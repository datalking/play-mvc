package com.github.datalking.io;

import com.github.datalking.common.DefaultPropertiesPersister;
import com.github.datalking.common.PropertiesPersister;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * properties文件操作工具类
 *
 * @author yaoo on 5/28/18
 */
public abstract class PropertiesLoaderUtils {

    private static final String XML_FILE_EXTENSION = ".xml";

    public static Properties loadProperties(EncodedResource resource) {
        Properties props = new Properties();

        fillProperties(props, resource);

        return props;
    }

    public static void fillProperties(Properties props, EncodedResource resource) {

        fillProperties(props, resource, new DefaultPropertiesPersister());
    }

    static void fillProperties(Properties props, EncodedResource resource, PropertiesPersister persister) {

        InputStream stream = null;
        Reader reader = null;
        try {
            String filename = resource.getResource().getFilename();

            if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
                stream = resource.getInputStream();

                // 加载xml文件
                persister.loadFromXml(props, stream);
            } else if (resource.requiresReader()) {
                reader = resource.getReader();

                persister.load(props, reader);
            } else {
                stream = resource.getInputStream();

                persister.load(props, stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 加载properties文件
     */
    public static Properties loadProperties(Resource resource) throws IOException {
        Properties props = new Properties();

        fillProperties(props, resource);
        return props;
    }

    /**
     * Fill the given properties from the given resource (in ISO-8859-1 encoding).
     *
     * @param props    the Properties instance to fill
     * @param resource the resource to load from
     */
    public static void fillProperties(Properties props, Resource resource) throws IOException {
        InputStream is = resource.getInputStream();
        try {
            String filename = resource.getFilename();
            if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {

                props.loadFromXML(is);
            } else {

                props.load(is);
            }
        } finally {
            is.close();
        }
    }

    /**
     * Load all properties from specified class path resource (in ISO-8859-1 encoding), using default class loader.
     * Merges properties if more than one resource of the same name found in the class path.
     *
     * @param resourceName the name of the class path resource
     * @return the populated Properties instance
     */
    public static Properties loadAllProperties(String resourceName) throws IOException {
        return loadAllProperties(resourceName, null);
    }

    public static Properties loadAllProperties(String resourceName, ClassLoader classLoader) throws IOException {
        Assert.notNull(resourceName, "Resource name must not be null");
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = ClassUtils.getDefaultClassLoader();
        }
        Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
                ClassLoader.getSystemResources(resourceName));
        Properties props = new Properties();

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection con = url.openConnection();
//            ResourceUtils.useCachesIfNecessary(con);
            InputStream is = con.getInputStream();
            try {
                if (resourceName != null && resourceName.endsWith(XML_FILE_EXTENSION)) {

                    props.loadFromXML(is);
                } else {

                    props.load(is);
                }
            } finally {
                is.close();
            }
        }
        return props;
    }

}
