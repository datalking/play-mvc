package com.github.datalking.web.mvc;

import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.web.servlet.RequestToViewNameTranslator;

import javax.servlet.http.HttpServletRequest;

/**
 *
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

    public void setPrefix(String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    public void setSuffix(String suffix) {
        this.suffix = (suffix != null ? suffix : "");
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setStripLeadingSlash(boolean stripLeadingSlash) {
        this.stripLeadingSlash = stripLeadingSlash;
    }

    public void setStripTrailingSlash(boolean stripTrailingSlash) {
        this.stripTrailingSlash = stripTrailingSlash;
    }

    public void setStripExtension(boolean stripExtension) {
        this.stripExtension = stripExtension;
    }

    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
    }

    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
    }

    public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
        this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
    }

    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    /**
     * 获取视图名的请求路径
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
