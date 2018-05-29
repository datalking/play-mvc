package com.github.datalking.web.servlet.handler;

import com.github.datalking.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 拦截以非注解形式注册的url，多用于xml
 *
 * @author yaoo on 5/4/18
 */
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {

    /**
     * url -> 对应的controller对象
     */
    private final Map<String, Object> urlMap = new HashMap<>();

    public void setMappings(Properties mappings) {
        CollectionUtils.mergePropertiesIntoMap(mappings, this.urlMap);
    }

    public void setUrlMap(Map<String, ?> urlMap) {
        this.urlMap.putAll(urlMap);
    }

    public Map<String, ?> getUrlMap() {
        return this.urlMap;
    }

    @Override
    public void initApplicationContext() {
        super.initApplicationContext();
        registerHandlers(this.urlMap);
    }

    protected void registerHandlers(Map<String, Object> urlMap) {
        if (urlMap.isEmpty()) {

            logger.warn("Neither 'urlMap' nor 'mappings' set on SimpleUrlHandlerMapping");
        } else {
            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                String url = entry.getKey();
                Object handler = entry.getValue();

                if (!url.startsWith("/")) {
                    url = "/" + url;
                }

                if (handler instanceof String) {
                    handler = ((String) handler).trim();
                }

                registerHandler(url, handler);
            }
        }
    }

}
