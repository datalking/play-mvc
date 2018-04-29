package com.github.datalking.web.http;

import com.github.datalking.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 4/29/18
 */
public class ServletServerHttpResponse implements HttpOutputMessage, Closeable {

    private final HttpServletResponse servletResponse;

    private final HttpHeaders headers = new HttpHeaders();

    private boolean headersWritten = false;

    public ServletServerHttpResponse(HttpServletResponse servletResponse) {
        Assert.notNull(servletResponse, "'servletResponse' must not be null");
        this.servletResponse = servletResponse;
    }

    public HttpServletResponse getServletResponse() {
        return this.servletResponse;
    }

    public void setStatusCode(HttpStatus status) {
        this.servletResponse.setStatus(status.value());
    }

    public HttpHeaders getHeaders() {
        return (this.headersWritten ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
    }

    public OutputStream getBody() throws IOException {
        writeHeaders();
        return this.servletResponse.getOutputStream();
    }

    public void close() {
        writeHeaders();
    }

    private void writeHeaders() {
        if (!this.headersWritten) {
            for (Map.Entry<String, List<String>> entry : this.headers.entrySet()) {
                String headerName = entry.getKey();
                for (String headerValue : entry.getValue()) {
                    this.servletResponse.addHeader(headerName, headerValue);
                }
            }
            // HttpServletResponse exposes some headers as properties: we should include those if not already present
            if (this.servletResponse.getContentType() == null && this.headers.getContentType() != null) {
                this.servletResponse.setContentType(this.headers.getContentType().toString());
            }
            if (this.servletResponse.getCharacterEncoding() == null && this.headers.getContentType() != null &&
                    this.headers.getContentType().getCharSet() != null) {
                this.servletResponse.setCharacterEncoding(this.headers.getContentType().getCharSet().name());
            }
            this.headersWritten = true;
        }
    }

}
