package com.github.datalking.web.http;

import com.github.datalking.common.LinkedCaseInsensitiveMap;
import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 4/29/18
 */
public class ServletServerHttpRequest implements HttpRequest, HttpInputMessage {

    protected static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    protected static final String FORM_CHARSET = "UTF-8";

    private static final String METHOD_POST = "POST";

    private final HttpServletRequest servletRequest;

    private HttpHeaders headers;

    public ServletServerHttpRequest(HttpServletRequest servletRequest) {
        Assert.notNull(servletRequest, "HttpServletRequest must not be null");
        this.servletRequest = servletRequest;
    }

    public HttpServletRequest getServletRequest() {
        return this.servletRequest;
    }

    public HttpMethod getMethod() {
        return HttpMethod.valueOf(this.servletRequest.getMethod());
    }

    public URI getURI() {
        try {
            return new URI(this.servletRequest.getScheme(), null, this.servletRequest.getServerName(),
                    this.servletRequest.getServerPort(), this.servletRequest.getRequestURI(),
                    this.servletRequest.getQueryString(), null);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Could not get HttpServletRequest URI: " + ex.getMessage(), ex);
        }
    }

    public HttpHeaders getHeaders() {
        if (this.headers == null) {
            this.headers = new HttpHeaders();
            for (Enumeration<?> headerNames = this.servletRequest.getHeaderNames(); headerNames.hasMoreElements(); ) {
                String headerName = (String) headerNames.nextElement();
                for (Enumeration<?> headerValues = this.servletRequest.getHeaders(headerName);
                     headerValues.hasMoreElements(); ) {
                    String headerValue = (String) headerValues.nextElement();
                    this.headers.add(headerName, headerValue);
                }
            }
            // HttpServletRequest exposes some headers as properties: we should include those if not already present
            MediaType contentType = this.headers.getContentType();
            if (contentType == null) {
                String requestContentType = this.servletRequest.getContentType();
                if (StringUtils.hasLength(requestContentType)) {
                    contentType = MediaType.parseMediaType(requestContentType);
                    this.headers.setContentType(contentType);
                }
            }
            if (contentType != null && contentType.getCharSet() == null) {
                String requestEncoding = this.servletRequest.getCharacterEncoding();
                if (StringUtils.hasLength(requestEncoding)) {
                    Charset charSet = Charset.forName(requestEncoding);
                    Map<String, String> params = new LinkedCaseInsensitiveMap<>();
                    params.putAll(contentType.getParameters());
                    params.put("charset", charSet.toString());
                    MediaType newContentType = new MediaType(contentType.getType(), contentType.getSubtype(), params);
                    this.headers.setContentType(newContentType);
                }
            }
            if (this.headers.getContentLength() == -1) {
                int requestContentLength = this.servletRequest.getContentLength();
                if (requestContentLength != -1) {
                    this.headers.setContentLength(requestContentLength);
                }
            }
        }
        return this.headers;
    }

    public InputStream getBody() throws IOException {
        if (isFormPost(this.servletRequest)) {
            return getBodyFromServletRequestParameters(this.servletRequest);
        } else {
            return this.servletRequest.getInputStream();
        }
    }

    private static boolean isFormPost(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
                METHOD_POST.equalsIgnoreCase(request.getMethod()));
    }

    private static InputStream getBodyFromServletRequestParameters(HttpServletRequest request) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        Writer writer = new OutputStreamWriter(bos, FORM_CHARSET);

        Map<String, String[]> form = request.getParameterMap();
        for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
            String name = nameIterator.next();
            List<String> values = Arrays.asList(form.get(name));
            for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                String value = valueIterator.next();
                writer.write(URLEncoder.encode(name, FORM_CHARSET));
                if (value != null) {
                    writer.write('=');
                    writer.write(URLEncoder.encode(value, FORM_CHARSET));
                    if (valueIterator.hasNext()) {
                        writer.write('&');
                    }
                }
            }
            if (nameIterator.hasNext()) {
                writer.append('&');
            }
        }
        writer.flush();

        return new ByteArrayInputStream(bos.toByteArray());
    }


}
