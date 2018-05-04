package com.github.datalking.web.support;

import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.io.ClassPathResource;
import com.github.datalking.io.Resource;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.ResourceUtils;
import com.github.datalking.util.StreamUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.HttpRequestHandler;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.servlet.HandlerMapping;
import com.github.datalking.web.servlet.ServletWebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * @author yaoo on 5/4/18
 */
public class ResourceHttpRequestHandler extends WebContentGenerator implements HttpRequestHandler, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(ResourceHttpRequestHandler.class);

//    private static final boolean jafPresent = ClassUtils.isPresent("javax.activation.FileTypeMap", ResourceHttpRequestHandler.class.getClassLoader());

    private List<Resource> locations;


    public ResourceHttpRequestHandler() {
        super(METHOD_GET, METHOD_HEAD);
    }

    public void setLocations(List<Resource> locations) {
        Assert.notEmpty(locations.toArray(), "Locations list must not be empty");
        this.locations = locations;
    }

    public void afterPropertiesSet()  {
        if (logger.isWarnEnabled() && CollectionUtils.isEmpty(this.locations)) {
            logger.warn("Locations list is empty. No resources will be served");
        }
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        checkAndPrepare(request, response, true);

        // check whether a matching resource exists
        Resource resource = getResource(request);
        if (resource == null) {
            logger.debug("No matching resource found - returning 404");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // check the resource's media type
        MediaType mediaType = getMediaType(resource);
        if (mediaType != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Determined media type '" + mediaType + "' for " + resource);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No media type found for " + resource + " - not sending a content-type header");
            }
        }

        // header phase
//        if (new ServletWebRequest(request, response).checkNotModified(resource.lastModified())) {
//            logger.debug("Resource not modified - returning 304");
//            return;
//        }
        setHeaders(response, resource, mediaType);

        // content phase
        if (METHOD_HEAD.equals(request.getMethod())) {
            logger.trace("HEAD request - skipping content");
            return;
        }
        writeContent(response, resource);
    }

    protected Resource getResource(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (path == null) {
            throw new IllegalStateException("Required request attribute '" +
                    HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }
        path = processPath(path);
        if (!StringUtils.hasText(path) || isInvalidPath(path)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring invalid resource path [" + path + "]");
            }
            return null;
        }

        if (path.contains("%")) {
            try {
                // Use URLDecoder (vs UriUtils) to preserve potentially decoded UTF-8 chars
                if (isInvalidPath(URLDecoder.decode(path, "UTF-8"))) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Ignoring invalid resource path with escape sequences [" + path + "].");
                    }
                    return null;
                }
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                // ignore: shouldn't happen
            }
        }

//        for (Resource location : this.locations) {
//            try {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Trying relative path [" + path + "] against base location: " + location);
//                }
//                Resource resource = location.createRelative(path);
//                if (resource.exists() && resource.isReadable()) {
//                    if (isResourceUnderLocation(resource, location)) {
//                        if (logger.isDebugEnabled()) {
//                            logger.debug("Found matching resource: " + resource);
//                        }
//                        return resource;
//                    } else {
//                        if (logger.isTraceEnabled()) {
//                            logger.trace("resource=\"" + resource + "\" was successfully resolved " +
//                                    "but is not under the location=\"" + location);
//                        }
//                        return null;
//                    }
//                } else if (logger.isTraceEnabled()) {
//                    logger.trace("Relative resource doesn't exist or isn't readable: " + resource);
//                }
//            } catch (IOException ex) {
//                logger.debug("Failed to create relative resource - trying next resource location", ex);
//            }
//        }

        return null;
    }

    protected String processPath(String path) {
        boolean slash = false;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                slash = true;
            } else if (path.charAt(i) > ' ' && path.charAt(i) != 127) {
                if (i == 0 || (i == 1 && slash)) {
                    return path;
                }
                path = slash ? "/" + path.substring(i) : path.substring(i);
                if (logger.isTraceEnabled()) {
                    logger.trace("Path trimmed for leading '/' and control characters: " + path);
                }
                return path;
            }
        }
        return (slash ? "/" : "");
    }

    protected boolean isInvalidPath(String path) {
        if (logger.isTraceEnabled()) {
            logger.trace("Applying \"invalid path\" checks to path: " + path);
        }
        if (path.contains("WEB-INF") || path.contains("META-INF")) {
            if (logger.isTraceEnabled()) {
                logger.trace("Path contains \"WEB-INF\" or \"META-INF\".");
            }
            return true;
        }
        if (path.contains(":/")) {
            String relativePath = (path.charAt(0) == '/' ? path.substring(1) : path);
//            if (ResourceUtils.isUrl(relativePath) || relativePath.startsWith("url:")) {
            if (relativePath.startsWith("url:")) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Path represents URL or has \"url:\" prefix.");
                }
                return true;
            }
        }
        if (path.contains("..")) {
            path = StringUtils.cleanPath(path);
            if (path.contains("../")) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Path contains \"../\" after call to StringUtils#cleanPath.");
                }
                return true;
            }
        }
        return false;
    }

    private boolean isResourceUnderLocation(Resource resource, Resource location) throws IOException {
        if (!resource.getClass().equals(location.getClass())) {
            return false;
        }
        String resourcePath = "";
        String locationPath = "";

//        if (resource instanceof UrlResource) {
//            resourcePath = resource.getURL().toExternalForm();
//            locationPath = location.getURL().toExternalForm();
//        } else
            if (resource instanceof ClassPathResource) {
            resourcePath = ((ClassPathResource) resource).getPath();
            locationPath = ((ClassPathResource) location).getPath();
        }
//        else if (resource instanceof ServletContextResource) {
//            resourcePath = ((ServletContextResource) resource).getPath();
//            locationPath = ((ServletContextResource) location).getPath();
//        } else {
//            resourcePath = resource.getURL().getPath();
//            locationPath = location.getURL().getPath();
//        }
        if (locationPath.equals(resourcePath)) {
            return true;
        }
        locationPath = (locationPath.endsWith("/") ||
                !StringUtils.hasLength(locationPath) ? locationPath : locationPath + "/");
        if (!resourcePath.startsWith(locationPath)) {
            return false;
        }
        if (resourcePath.contains("%")) {
            // Use URLDecoder (vs UriUtils) to preserve potentially decoded UTF-8 chars...
            if (URLDecoder.decode(resourcePath, "UTF-8").contains("../")) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Resolved resource path contains \"../\" after decoding: " + resourcePath);
                }
                return false;
            }
        }
        return true;
    }

    protected MediaType getMediaType(Resource resource) {
        MediaType mediaType = null;
//        String mimeType = getServletContext().getMimeType(resource.getFilename());
        String mimeType = "";
        if (StringUtils.hasText(mimeType)) {
            mediaType = MediaType.parseMediaType(mimeType);
        }
//        if (jafPresent && (mediaType == null || MediaType.APPLICATION_OCTET_STREAM.equals(mediaType))) {
        if (mediaType == null || MediaType.APPLICATION_OCTET_STREAM.equals(mediaType)) {
//            MediaType jafMediaType = ActivationMediaTypeFactory.getMediaType(resource.getFilename());
            MediaType jafMediaType = ActivationMediaTypeFactory.getMediaType("filename");
            if (jafMediaType != null && !MediaType.APPLICATION_OCTET_STREAM.equals(jafMediaType)) {
                mediaType = jafMediaType;
            }
        }
        return mediaType;
    }

    protected void setHeaders(HttpServletResponse response, Resource resource, MediaType mediaType) throws IOException {
//        long length = resource.contentLength();
        long length = 12;
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Resource content too long (beyond Integer.MAX_VALUE): " + resource);
        }
        response.setContentLength((int) length);

        if (mediaType != null) {
            response.setContentType(mediaType.toString());
        }
    }

    protected void writeContent(HttpServletResponse response, Resource resource) throws IOException {
        InputStream in = resource.getInputStream();
        try {
            StreamUtils.copy(in, response.getOutputStream());
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    private static class ActivationMediaTypeFactory {

        private static final FileTypeMap fileTypeMap;

        static {
            fileTypeMap = loadFileTypeMapFromContextSupportModule();
        }

        private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
            // see if we can find the extended mime.types from the context-support module
            ClassPathResource mappingLocation = new ClassPathResource("org/springframework/mail/javamail/mime.types");
            if (mappingLocation.exists()) {
                InputStream inputStream = null;
                try {
                    inputStream = mappingLocation.getInputStream();
                    return new MimetypesFileTypeMap(inputStream);
                } catch (IOException ex) {
                    // ignore
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex) {
                            // ignore
                        }
                    }
                }
            }
            return FileTypeMap.getDefaultFileTypeMap();
        }

        public static MediaType getMediaType(String filename) {
            String mediaType = fileTypeMap.getContentType(filename);
            return (StringUtils.hasText(mediaType) ? MediaType.parseMediaType(mediaType) : null);
        }
    }

}
