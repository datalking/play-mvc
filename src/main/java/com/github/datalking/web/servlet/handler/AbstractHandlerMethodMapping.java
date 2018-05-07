package com.github.datalking.web.servlet.handler;

import com.github.datalking.beans.factory.BeanFactoryUtils;
import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.common.LinkedMultiValueMap;
import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.web.mvc.method.HandlerMethod;
import com.github.datalking.web.mvc.method.HandlerMethodSelector;
import com.github.datalking.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定义request和HandlerMethod之间的映射
 *
 * @author yaoo on 4/28/18
 */
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {

    private boolean detectHandlerMethodsInAncestorContexts = false;

    private final Map<T, HandlerMethod> handlerMethods = new LinkedHashMap<>();

    private final MultiValueMap<String, T> urlMap = new LinkedMultiValueMap<>();

    public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
        this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
    }

    public Map<T, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(this.handlerMethods);
    }

    /**
     * 注册请求url与方法的映射关系
     */
    @Override
    public void afterPropertiesSet() {
        initHandlerMethods();
    }

    protected void initHandlerMethods() {

        String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ?
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
                getApplicationContext().getBeanNamesForType(Object.class));

        for (String beanName : beanNames) {

            if (isHandler(getApplicationContext().getType(beanName))) {
                // 如果是请求handlerMapping，就检测该bean内的映射方法
                detectHandlerMethods(beanName);
            }
        }

        handlerMethodsInitialized(getHandlerMethods());
    }

    protected abstract boolean isHandler(Class<?> beanType);

    protected void detectHandlerMethods(final Object handler) {
        // 获取handler的class
        Class<?> handlerType = (handler instanceof String ? getApplicationContext().getType((String) handler) : handler.getClass());

        // Avoid repeated calls to getMappingForMethod which would rebuild RequestMatchingInfo instances
        final Map<Method, T> mappings = new IdentityHashMap<>();
        final Class<?> aClass = ClassUtils.getUserClass(handlerType);

        Set<Method> methods = HandlerMethodSelector.selectMethods(aClass, new ReflectionUtils.MethodFilter() {
            public boolean matches(Method method) {

                // 寻找aClass中的匹配的method方法的映射信息
                T mapping = getMappingForMethod(method, aClass);
                if (mapping != null) {
                    mappings.put(method, mapping);
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (Method method : methods) {
            registerHandlerMethod(handler, method, mappings.get(method));
        }
    }

    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
        HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
        HandlerMethod oldHandlerMethod = this.handlerMethods.get(mapping);
        if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
            throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + newHandlerMethod.getBean() +
                    "' bean method \n" + newHandlerMethod + "\nto " + mapping + ": There is already '" +
                    oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
        }

        this.handlerMethods.put(mapping, newHandlerMethod);

        Set<String> patterns = getMappingPathPatterns(mapping);
        for (String pattern : patterns) {
            if (!getPathMatcher().isPattern(pattern)) {
                this.urlMap.add(pattern, mapping);
            }
        }
    }

    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        HandlerMethod handlerMethod;
        if (handler instanceof String) {
            String beanName = (String) handler;
            handlerMethod = new HandlerMethod(beanName, getApplicationContext(), method);
        } else {
            handlerMethod = new HandlerMethod(handler, method);
        }
        return handlerMethod;
    }

    protected abstract Set<String> getMappingPathPatterns(T mapping);

    protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
    }


    /**
     * 匹配请求url的方法
     */
    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {

        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);

        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);

        return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
    }

    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        List<Match> matches = new ArrayList<>();
        List<T> directPathMatches = this.urlMap.get(lookupPath);
        if (directPathMatches != null) {
            addMatchingMappings(directPathMatches, matches, request);
        }
        if (matches.isEmpty()) {
            // No choice but to go through all mappings...
            addMatchingMappings(this.handlerMethods.keySet(), matches, request);
        }

        if (!matches.isEmpty()) {
            Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
            Collections.sort(matches, comparator);
            if (logger.isTraceEnabled()) {
                logger.trace("Found " + matches.size() + " matching mapping(s) for [" + lookupPath + "] : " + matches);
            }
            Match bestMatch = matches.get(0);
            if (matches.size() > 1) {
                Match secondBestMatch = matches.get(1);
                if (comparator.compare(bestMatch, secondBestMatch) == 0) {
                    Method m1 = bestMatch.handlerMethod.getMethod();
                    Method m2 = secondBestMatch.handlerMethod.getMethod();
                    throw new IllegalStateException(
                            "Ambiguous handler methods mapped for HTTP path '" + request.getRequestURL() + "': {" +
                                    m1 + ", " + m2 + "}");
                }
            }
            handleMatch(bestMatch.mapping, lookupPath, request);
            return bestMatch.handlerMethod;
        } else {
            return handleNoMatch(handlerMethods.keySet(), lookupPath, request);
        }
    }

    private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpServletRequest request) {
        for (T mapping : mappings) {
            T match = getMatchingMapping(mapping, request);
            if (match != null) {
                matches.add(new Match(match, this.handlerMethods.get(mapping)));
            }
        }
    }

    protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);


    protected abstract Comparator<T> getMappingComparator(HttpServletRequest request);

    // 添加属性
    protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
    }


    protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request) {
        return null;
    }

    private class Match {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        public Match(T mapping, HandlerMethod handlerMethod) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return this.mapping.toString();
        }
    }


    private class MatchComparator implements Comparator<Match> {

        private final Comparator<T> comparator;

        public MatchComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        public int compare(Match match1, Match match2) {
            return this.comparator.compare(match1.mapping, match2.mapping);
        }
    }

}
