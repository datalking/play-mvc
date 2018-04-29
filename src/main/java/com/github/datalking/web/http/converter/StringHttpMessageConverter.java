package com.github.datalking.web.http.converter;

import com.github.datalking.util.StreamUtils;
import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.HttpOutputMessage;
import com.github.datalking.web.http.MediaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yaoo on 4/29/18
 */
public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

    private final Charset defaultCharset;

    private final List<Charset> availableCharsets;

    private boolean writeAcceptCharset = true;

    public StringHttpMessageConverter() {
        this(DEFAULT_CHARSET);
    }

    public StringHttpMessageConverter(Charset defaultCharset) {
        super(new MediaType("text", "plain", defaultCharset), MediaType.ALL);
        this.defaultCharset = defaultCharset;
        this.availableCharsets = new ArrayList<Charset>(Charset.availableCharsets().values());
    }

    public void setWriteAcceptCharset(boolean writeAcceptCharset) {
        this.writeAcceptCharset = writeAcceptCharset;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
        Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
        return StreamUtils.copyToString(inputMessage.getBody(), charset);
    }

    @Override
    protected Long getContentLength(String str, MediaType contentType) {
        Charset charset = getContentTypeCharset(contentType);
        try {
            return (long) str.getBytes(charset.name()).length;
        }
        catch (UnsupportedEncodingException ex) {
            // should not occur
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException {
        if (this.writeAcceptCharset) {
            outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
        }
        Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());
        StreamUtils.copy(str, charset, outputMessage.getBody());
    }


    protected List<Charset> getAcceptedCharsets() {
        return this.availableCharsets;
    }

    private Charset getContentTypeCharset(MediaType contentType) {
        if (contentType != null && contentType.getCharSet() != null) {
            return contentType.getCharSet();
        }
        else {
            return this.defaultCharset;
        }
    }

}
