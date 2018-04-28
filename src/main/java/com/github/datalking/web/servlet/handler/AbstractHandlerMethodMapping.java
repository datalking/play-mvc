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

import javax.servlet.ServletException;
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

    /**
     * Return a map with all handler methods and their mappings.
     */
    public Map<T, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(this.handlerMethods);
    }

    /**
     * Detects handler methods at initialization.
     */
    public void afterPropertiesSet() {
        initHandlerMethods();
    }

    /**
     * Scan beans in the ApplicationContext, detect and register handler methods.
     *
     * @see #isHandler(Class)
     * @see #getMappingForMethod(Method, Class)
     * @see #handlerMethodsInitialized(Map)
     */
    protected void initHandlerMethods() {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for request mappings in application context: " + getApplicationContext());
        }


        String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ?
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
                getApplicationContext().getBeanNamesForType(Object.class));

        for (String beanName : beanNames) {
            if (isHandler(getApplicationContext().getType(beanName))) {
                detectHandlerMethods(beanName);
            }
        }
        handlerMethodsInitialized(getHandlerMethods());
    }

    /**
     * Whether the given type is a handler with handler methods.
     *
     * @param beanType the type of the bean being checked
     * @return "true" if this a handler type, "false" otherwise.
     */
    protected abstract boolean isHandler(Class<?> beanType);

    /**
     * Look for handler methods in a handler.
     *
     * @param handler the bean name of a handler or a handler instance
     */
    protected void detectHandlerMethods(final Object handler) {
        Class<?> handlerType =
                (handler instanceof String ? getApplicationContext().getType((String) handler) : handler.getClass());

        // Avoid repeated calls to getMappingForMethod which would rebuild RequestMatchingInfo instances
        final Map<Method, T> mappings = new IdentityHashMap<Method, T>();
        final Class<?> userType = ClassUtils.getUserClass(handlerType);

        Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
            public boolean matches(Method method) {
                T mapping = getMappingForMethod(method, userType);
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

    /**
     * Provide the mapping for a handler method. A method for which no
     * mapping can be provided is not a handler method.
     *
     * @param method      the method to provide a mapping for
     * @param handlerType the handler type, possibly a sub-type of the method's
     *                    declaring class
     * @return the mapping, or {@code null} if the method is not mapped
     */
    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    /**
     * Register a handler method and its unique mapping.
     *
     * @param handler the bean name of the handler or the handler instance
     * @param method  the method to register
     * @param mapping the mapping conditions associated with the handler method
     * @throws IllegalStateException if another method was already registered
     *                               under the same mapping
     */
    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
        HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
        HandlerMethod oldHandlerMethod = this.handlerMethods.get(mapping);
        if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
            throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + newHandlerMethod.getBean() +
                    "' bean method \n" + newHandlerMethod + "\nto " + mapping + ": There is already '" +
                    oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
        }

        this.handlerMethods.put(mapping, newHandlerMethod);
        if (logger.isInfoEnabled()) {
            logger.info("Mapped \"" + mapping + "\" onto " + newHandlerMethod);
        }

        Set<String> patterns = getMappingPathPatterns(mapping);
        for (String pattern : patterns) {
            if (!getPathMatcher().isPattern(pattern)) {
                this.urlMap.add(pattern, mapping);
            }
        }
    }

    /**
     * Create the HandlerMethod instance.
     *
     * @param handler either a bean name or an actual handler instance
     * @param method  the target method
     * @return the created HandlerMethod
     */
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

    /**
     * Extract and return the URL paths contained in a mapping.
     */
    protected abstract Set<String> getMappingPathPatterns(T mapping);

    /**
     * Invoked after all handler methods have been detected.
     *
     * @param handlerMethods a read-only map with handler methods and mappings.
     */
    protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
    }


    /**
     * Look up a handler method for the given request.
     */
    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up handler method for path " + lookupPath);
        }
        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
        if (logger.isDebugEnabled()) {
            if (handlerMethod != null) {
                logger.debug("Returning handler method [" + handlerMethod + "]");
            } else {
                logger.debug("Did not find handler method for [" + lookupPath + "]");
            }
        }
        return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
    }

    /**
     * Look up the best-matching handler method for the current request.
     * If multiple matches are found, the best match is selected.
     *
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     * @return the best-matching handler method, or {@code null} if no match
     * @see #handleMatch(Object, String, HttpServletRequest)
     * @see #handleNoMatch(Set, String, HttpServletRequest)
     */
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        List<Match> matches = new ArrayList<Match>();
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


    protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
    }


    protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request)
            throws Exception {

        return null;
    }


    /**
     * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
     * comparing the best match with a comparator in the context of the current request.
     */
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
