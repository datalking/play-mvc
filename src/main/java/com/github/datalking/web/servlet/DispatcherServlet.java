package com.github.datalking.web.servlet;

import java.util.List;

/**
 * http请求的中心控制器
 *
 * @author yaoo on 4/25/18
 */
public class DispatcherServlet extends FrameworkServlet{

    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";
    public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";
    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";
    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";
    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";
    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    private boolean detectAllHandlerMappings = true;
    private boolean detectAllViewResolvers = true;
    private boolean detectAllHandlerAdapters = true;

    //private MultipartResolver multipartResolver;
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private RequestToViewNameTranslator viewNameTranslator;
    private List<ViewResolver> viewResolvers;

    public DispatcherServlet() {
        super();
    }




}
