package com.github.datalking.web.mvc;

import com.github.datalking.beans.factory.BeanNameAware;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.web.context.WebApplicationObjectSupport;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.support.RequestContext;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * View抽象类作为基类
 * 子类推荐使用javabean，方便使用容器管理
 *
 * @author yaoo on 4/26/18
 */
public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";

    private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;

    private String beanName;

    private String contentType = DEFAULT_CONTENT_TYPE;

    private String requestContextAttribute;

    private final Map<String, Object> staticAttributes = new LinkedHashMap<>();

    private boolean exposePathVariables = true;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setRequestContextAttribute(String requestContextAttribute) {
        this.requestContextAttribute = requestContextAttribute;
    }

    public String getRequestContextAttribute() {
        return this.requestContextAttribute;
    }

    public void setAttributesCSV(String propString) throws IllegalArgumentException {
        if (propString != null) {
            StringTokenizer st = new StringTokenizer(propString, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                int eqIdx = tok.indexOf("=");
                if (eqIdx == -1) {
                    throw new IllegalArgumentException("Expected = in attributes CSV string '" + propString + "'");
                }
                if (eqIdx >= tok.length() - 2) {
                    throw new IllegalArgumentException(
                            "At least 2 characters ([]) required in attributes CSV string '" + propString + "'");
                }
                String name = tok.substring(0, eqIdx);
                String value = tok.substring(eqIdx + 1);

                // Delete first and last characters of value: { and }
                value = value.substring(1);
                value = value.substring(0, value.length() - 1);

                addStaticAttribute(name, value);
            }
        }
    }

    public void setAttributes(Properties attributes) {
        CollectionUtils.mergePropertiesIntoMap(attributes, this.staticAttributes);
    }

    public void setAttributesMap(Map<String, ?> attributes) {
        if (attributes != null) {
            for (Map.Entry<String, ?> entry : attributes.entrySet()) {
                addStaticAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<String, Object> getAttributesMap() {
        return this.staticAttributes;
    }


    public void addStaticAttribute(String name, Object value) {
        this.staticAttributes.put(name, value);
    }

    public Map<String, Object> getStaticAttributes() {
        return Collections.unmodifiableMap(this.staticAttributes);
    }

    public void setExposePathVariables(boolean exposePathVariables) {
        this.exposePathVariables = exposePathVariables;
    }

    public boolean isExposePathVariables() {
        return this.exposePathVariables;
    }


    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
        if (logger.isTraceEnabled()) {
            logger.trace("Rendering view with name '" + this.beanName + "' with model " + model + " and static attributes " + this.staticAttributes);
        }

        // 静态属性合并
        Map<String, Object> mergedModel = createMergedOutputModel(model, request, response);
        // 处理下载内容，默认执行nothing
        prepareResponse(request, response);
        // 渲染视图
        renderMergedOutputModel(mergedModel, request, response);
    }


    protected Map<String, Object> createMergedOutputModel(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> pathVars = (this.exposePathVariables ? (Map<String, Object>) request.getAttribute(View.PATH_VARIABLES) : null);
        // Consolidate static and dynamic model attributes.
        int size = this.staticAttributes.size();
        size += (model != null ? model.size() : 0);
        size += (pathVars != null ? pathVars.size() : 0);

        Map<String, Object> mergedModel = new LinkedHashMap<>(size);
        mergedModel.putAll(this.staticAttributes);
        if (pathVars != null) {
            mergedModel.putAll(pathVars);
        }
        if (model != null) {
            mergedModel.putAll(model);
        }

        if (this.requestContextAttribute != null) {
            mergedModel.put(this.requestContextAttribute, createRequestContext(request, response, mergedModel));
        }

        return mergedModel;
    }

    protected RequestContext createRequestContext(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {

        return new RequestContext(request, response, getServletContext(), model);
    }

    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        // 默认为false
        if (generatesDownloadContent()) {
            response.setHeader("Pragma", "private");
            response.setHeader("Cache-Control", "private, must-revalidate");
        }
    }

    protected boolean generatesDownloadContent() {
        return false;
    }

    protected abstract void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response);

    protected void exposeModelAsRequestAttributes(Map<String, Object> model, HttpServletRequest request) {
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            String modelName = entry.getKey();
            Object modelValue = entry.getValue();
            if (modelValue != null) {
                request.setAttribute(modelName, modelValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() +
                            "] to request in view with name '" + getBeanName() + "'");
                }
            } else {
                request.removeAttribute(modelName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Removed model object '" + modelName +
                            "' from request in view with name '" + getBeanName() + "'");
                }
            }
        }
    }

    protected ByteArrayOutputStream createTemporaryOutputStream() {
        return new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
    }

    protected void writeToResponse(HttpServletResponse response, ByteArrayOutputStream baos) throws IOException {
        // Write content type and also length (determined via byte array).
        response.setContentType(getContentType());
        response.setContentLength(baos.size());

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        baos.writeTo(out);
        out.flush();
    }

    protected void setResponseContentType(HttpServletRequest request, HttpServletResponse response) {
        MediaType mediaType = (MediaType) request.getAttribute(View.SELECTED_CONTENT_TYPE);
        if (mediaType != null && mediaType.isConcrete()) {
            response.setContentType(mediaType.toString());
        } else {
            response.setContentType(getContentType());
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        if (getBeanName() != null) {
            sb.append(": name '").append(getBeanName()).append("'");
        } else {
            sb.append(": unnamed");
        }
        return sb.toString();
    }

}
