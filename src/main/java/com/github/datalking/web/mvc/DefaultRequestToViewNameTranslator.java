package com.github.datalking.web.mvc;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yaoo on 4/28/18
 */
public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {

    private static final String SLASH = "/";


    private String prefix = "";

    private String suffix = "";

    private String separator = SLASH;

    private boolean stripLeadingSlash = true;

    private boolean stripTrailingSlash = true;

    private boolean stripExtension = true;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();


    /**
     * Set the prefix to prepend to generated view names.
     * @param prefix the prefix to prepend to generated view names
     */
    public void setPrefix(String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    /**
     * Set the suffix to append to generated view names.
     * @param suffix the suffix to append to generated view names
     */
    public void setSuffix(String suffix) {
        this.suffix = (suffix != null ? suffix : "");
    }

    /**
     * Set the value that will replace '{@code /}' as the separator
     * in the view name. The default behavior simply leaves '{@code /}'
     * as the separator.
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Set whether or not leading slashes should be stripped from the URI when
     * generating the view name. Default is "true".
     */
    public void setStripLeadingSlash(boolean stripLeadingSlash) {
        this.stripLeadingSlash = stripLeadingSlash;
    }

    /**
     * Set whether or not trailing slashes should be stripped from the URI when
     * generating the view name. Default is "true".
     */
    public void setStripTrailingSlash(boolean stripTrailingSlash) {
        this.stripTrailingSlash = stripTrailingSlash;
    }

    /**
     * Set whether or not file extensions should be stripped from the URI when
     * generating the view name. Default is "true".
     */
    public void setStripExtension(boolean stripExtension) {
        this.stripExtension = stripExtension;
    }

    /**
     * Set if URL lookup should always use the full path within the current servlet
     * context. Else, the path within the current servlet mapping is used
     * if applicable (i.e. in the case of a ".../*" servlet mapping in web.xml).
     * Default is "false".
     * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
     */
    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
    }

    /**
     * Set if the context path and request URI should be URL-decoded.
     * Both are returned <i>undecoded</i> by the Servlet API,
     * in contrast to the servlet path.
     * <p>Uses either the request encoding or the default encoding according
     * to the Servlet spec (ISO-8859-1).
     * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
     */
    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
    }

    /**
     * Set if ";" (semicolon) content should be stripped from the request URI.
     * @see org.springframework.web.util.UrlPathHelper#setRemoveSemicolonContent(boolean)
     */
    public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
        this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
    }

    /**
     * Set the {@link org.springframework.web.util.UrlPathHelper} to use for
     * the resolution of lookup paths.
     * <p>Use this to override the default UrlPathHelper with a custom subclass,
     * or to share common UrlPathHelper settings across multiple web components.
     */
    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }


    /**
     * Translates the request URI of the incoming {@link HttpServletRequest}
     * into the view name based on the configured parameters.
     * @see org.springframework.web.util.UrlPathHelper#getLookupPathForRequest
     * @see #transformPath
     */
    public String getViewName(HttpServletRequest request) {
        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
        return (this.prefix + transformPath(lookupPath) + this.suffix);
    }


    protected String transformPath(String lookupPath) {
        String path = lookupPath;
        if (this.stripLeadingSlash && path.startsWith(SLASH)) {
            path = path.substring(1);
        }
        if (this.stripTrailingSlash && path.endsWith(SLASH)) {
            path = path.substring(0, path.length() - 1);
        }
        if (this.stripExtension) {
            path = StringUtils.stripFilenameExtension(path);
        }
        if (!SLASH.equals(this.separator)) {
            path = StringUtils.replace(path, SLASH, this.separator);
        }
        return path;
    }

}
