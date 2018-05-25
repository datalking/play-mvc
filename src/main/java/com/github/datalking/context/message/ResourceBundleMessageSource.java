package com.github.datalking.context.message;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author yaoo on 5/7/18
 */
public class ResourceBundleMessageSource extends AbstractMessageSource {

    private String[] basenames = new String[0];

    private String defaultEncoding;

    private boolean fallbackToSystemLocale = true;

    private long cacheMillis = -1;

    private ClassLoader bundleClassLoader;

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    private final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles = new HashMap<>();

    private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMsgFormats = new HashMap<>();

    public void setBasename(String basename) {
        setBasenames(basename);
    }

    public void setBasenames(String... basenames) {
        if (basenames != null) {
            this.basenames = new String[basenames.length];
            for (int i = 0; i < basenames.length; i++) {
                String basename = basenames[i];
                Assert.hasText(basename, "Basename must not be empty");
                this.basenames[i] = basename.trim();
            }
        } else {
            this.basenames = new String[0];
        }
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
        this.fallbackToSystemLocale = fallbackToSystemLocale;
    }

    public void setCacheSeconds(int cacheSeconds) {
        this.cacheMillis = (cacheSeconds * 1000);
    }

    public void setBundleClassLoader(ClassLoader classLoader) {
        this.bundleClassLoader = classLoader;
    }

    protected ClassLoader getBundleClassLoader() {
        return (this.bundleClassLoader != null ? this.bundleClassLoader : this.beanClassLoader);
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        String result = null;
        for (int i = 0; result == null && i < this.basenames.length; i++) {
            ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
            if (bundle != null) {
                result = getStringOrNull(bundle, code);
            }
        }
        return result;
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        MessageFormat messageFormat = null;
        for (int i = 0; messageFormat == null && i < this.basenames.length; i++) {
            ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
            if (bundle != null) {
                messageFormat = getMessageFormat(bundle, code, locale);
            }
        }
        return messageFormat;
    }

    protected ResourceBundle getResourceBundle(String basename, Locale locale) {
        if (this.cacheMillis >= 0) {
            // Fresh ResourceBundle.getBundle call in order to let ResourceBundle
            // do its native caching, at the expense of more extensive lookup steps.
            return doGetBundle(basename, locale);
        } else {
            // Cache forever: prefer locale cache over repeated getBundle calls.
            synchronized (this.cachedResourceBundles) {
                Map<Locale, ResourceBundle> localeMap = this.cachedResourceBundles.get(basename);
                if (localeMap != null) {
                    ResourceBundle bundle = localeMap.get(locale);
                    if (bundle != null) {
                        return bundle;
                    }
                }
                try {
                    ResourceBundle bundle = doGetBundle(basename, locale);
                    if (localeMap == null) {
                        localeMap = new HashMap<>();
                        this.cachedResourceBundles.put(basename, localeMap);
                    }
                    localeMap.put(locale, bundle);
                    return bundle;
                } catch (MissingResourceException ex) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("ResourceBundle [" + basename + "] not found for MessageSource: " + ex.getMessage());
                    }
                    // Assume bundle not found -> do NOT throw the exception to allow for checking parent message source.
                    return null;
                }
            }
        }
    }

    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        if ((this.defaultEncoding != null && !"ISO-8859-1".equals(this.defaultEncoding)) ||
                !this.fallbackToSystemLocale || this.cacheMillis >= 0) {
//            if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_16) {
//                throw new IllegalStateException("Cannot use 'defaultEncoding', 'fallbackToSystemLocale' and " +
//                        "'cacheSeconds' on the standard ResourceBundleMessageSource when running on Java 5. " +
//                        "Consider using ReloadableResourceBundleMessageSource instead.");
//            }
            return new ControlBasedResourceBundleFactory().getBundle(basename, locale);
        } else {
            // Good old standard call...
            return ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
        }
    }

    protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
            throws MissingResourceException {

        synchronized (this.cachedBundleMsgFormats) {
            Map<String, Map<Locale, MessageFormat>> codeMap = this.cachedBundleMsgFormats.get(bundle);
            Map<Locale, MessageFormat> localeMap = null;
            if (codeMap != null) {
                localeMap = codeMap.get(code);
                if (localeMap != null) {
                    MessageFormat result = localeMap.get(locale);
                    if (result != null) {
                        return result;
                    }
                }
            }

            String msg = getStringOrNull(bundle, code);
            if (msg != null) {
                if (codeMap == null) {
                    codeMap = new HashMap<>();
                    this.cachedBundleMsgFormats.put(bundle, codeMap);
                }
                if (localeMap == null) {
                    localeMap = new HashMap<>();
                    codeMap.put(code, localeMap);
                }
                MessageFormat result = createMessageFormat(msg, locale);
                localeMap.put(locale, result);
                return result;
            }

            return null;
        }
    }

    private String getStringOrNull(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            // Assume key not found
            // -> do NOT throw the exception to allow for checking parent message source.
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + ": basenames=[" + StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
    }

    private class ControlBasedResourceBundleFactory {

        public ResourceBundle getBundle(String basename, Locale locale) {
            return ResourceBundle.getBundle(basename, locale, getBundleClassLoader(), new MessageSourceControl());
        }
    }

    private class MessageSourceControl extends ResourceBundle.Control {

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {

            // Special handling of default encoding
            if (format.equals("java.properties")) {
                String bundleName = toBundleName(baseName, locale);
                final String resourceName = toResourceName(bundleName, "properties");
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream;
                try {
                    stream = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<InputStream>() {
                                public InputStream run() throws IOException {
                                    InputStream is = null;
                                    if (reloadFlag) {
                                        URL url = classLoader.getResource(resourceName);
                                        if (url != null) {
                                            URLConnection connection = url.openConnection();
                                            if (connection != null) {
                                                connection.setUseCaches(false);
                                                is = connection.getInputStream();
                                            }
                                        }
                                    } else {
                                        is = classLoader.getResourceAsStream(resourceName);
                                    }
                                    return is;
                                }
                            });
                } catch (PrivilegedActionException ex) {
                    throw (IOException) ex.getException();
                }
                if (stream != null) {
                    try {
                        return (defaultEncoding != null ?
                                new PropertyResourceBundle(new InputStreamReader(stream, defaultEncoding)) :
                                new PropertyResourceBundle(stream));
                    } finally {
                        stream.close();
                    }
                } else {
                    return null;
                }
            } else {
                // Delegate handling of "java.class" format to standard Control
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return (fallbackToSystemLocale ? super.getFallbackLocale(baseName, locale) : null);
        }

        @Override
        public long getTimeToLive(String baseName, Locale locale) {
            return (cacheMillis >= 0 ? cacheMillis : super.getTimeToLive(baseName, locale));
        }

        @Override
        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
            if (super.needsReload(baseName, locale, format, loader, bundle, loadTime)) {
                cachedBundleMsgFormats.remove(bundle);
                return true;
            } else {
                return false;
            }
        }
    }

}
