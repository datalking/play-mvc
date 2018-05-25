package com.github.datalking.web.http.accept;

import com.github.datalking.io.ClassPathResource;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * 使用url path后缀决定请求的media type
 */
public class PathExtensionContentNegotiationStrategy extends AbstractMappingContentNegotiationStrategy {

    private static final boolean JAF_PRESENT = ClassUtils.isPresent("javax.activation.FileTypeMap",
            PathExtensionContentNegotiationStrategy.class.getClassLoader());

    private static final Logger logger = LoggerFactory.getLogger(PathExtensionContentNegotiationStrategy.class);

    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();

    static {
        urlPathHelper.setUrlDecode(false);
    }

    private boolean useJaf = true;

    public PathExtensionContentNegotiationStrategy(Map<String, MediaType> mediaTypes) {
        super(mediaTypes);
    }

    public PathExtensionContentNegotiationStrategy() {
        super(null);
    }

    public void setUseJaf(boolean useJaf) {
        this.useJaf = useJaf;
    }

    @Override
    protected String getMediaTypeKey(WebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            logger.warn("An HttpServletRequest is required to determine the media type key");
            return null;
        }
        String path = urlPathHelper.getLookupPathForRequest(servletRequest);
        String filename = WebUtils.extractFullFilenameFromUrlPath(path);
        String extension = StringUtils.getFilenameExtension(filename);
        return (StringUtils.hasText(extension)) ? extension.toLowerCase(Locale.ENGLISH) : null;
    }

    @Override
    protected void handleMatch(String extension, MediaType mediaType) {
    }

    @Override
    protected MediaType handleNoMatch(WebRequest webRequest, String extension) {
        if (this.useJaf && JAF_PRESENT) {
            MediaType jafMediaType = JafMediaTypeFactory.getMediaType("file." + extension);
            if (jafMediaType != null && !MediaType.APPLICATION_OCTET_STREAM.equals(jafMediaType)) {
                return jafMediaType;
            }
        }
        return null;
    }


    /**
     * Inner class to avoid hard-coded dependency on JAF.
     */
    private static class JafMediaTypeFactory {

        private static final FileTypeMap fileTypeMap;

        static {
            fileTypeMap = initFileTypeMap();
        }

        private static FileTypeMap initFileTypeMap() {
            ClassPathResource resource = new ClassPathResource("mime.types");
            if (resource.exists()) {

                if (logger.isTraceEnabled()) {
                    logger.trace("Loading Java Activation Framework FileTypeMap from " + resource);
                }

                InputStream inputStream = null;
                try {
                    inputStream = resource.getInputStream();
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
            if (logger.isTraceEnabled()) {
                logger.trace("Loading default Java Activation Framework FileTypeMap");
            }
            return FileTypeMap.getDefaultFileTypeMap();
        }

        public static MediaType getMediaType(String filename) {
            String mediaType = fileTypeMap.getContentType(filename);
            return (StringUtils.hasText(mediaType) ? MediaType.parseMediaType(mediaType) : null);
        }
    }

}
