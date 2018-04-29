package com.github.datalking.web.mvc.method;

import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.RequestMethod;
import com.github.datalking.web.mvc.condition.NameValueExpression;
import com.github.datalking.web.mvc.condition.ParamsRequestCondition;
import com.github.datalking.web.servlet.HandlerMapping;
import com.github.datalking.web.servlet.handler.AbstractHandlerMethodMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yaoo on 4/28/18
 */
public abstract class RequestMappingInfoHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> {

    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
        return info.getPatternsCondition().getPatterns();
    }

    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpServletRequest request) {
        return info.getMatchingCondition(request);
    }

    @Override
    protected Comparator<RequestMappingInfo> getMappingComparator(final HttpServletRequest request) {
        return new Comparator<RequestMappingInfo>() {
            public int compare(RequestMappingInfo info1, RequestMappingInfo info2) {
                return info1.compareTo(info2, request);
            }
        };
    }

    @Override
    protected void handleMatch(RequestMappingInfo info, String lookupPath, HttpServletRequest request) {
        super.handleMatch(info, lookupPath, request);

        String bestPattern;
        Map<String, String> uriVariables;
        Map<String, String> decodedUriVariables;

        Set<String> patterns = info.getPatternsCondition().getPatterns();
        if (patterns.isEmpty()) {
            bestPattern = lookupPath;
            uriVariables = Collections.emptyMap();
            decodedUriVariables = Collections.emptyMap();
        } else {
            bestPattern = patterns.iterator().next();
            uriVariables = getPathMatcher().extractUriTemplateVariables(bestPattern, lookupPath);
            decodedUriVariables = getUrlPathHelper().decodePathVariables(request, uriVariables);
        }

        request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, decodedUriVariables);

        if (isMatrixVariableContentAvailable()) {
            Map<String, MultiValueMap<String, String>> matrixVars = extractMatrixVariables(request, uriVariables);
            request.setAttribute(HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE, matrixVars);
        }

        if (!info.getProducesCondition().getProducibleMediaTypes().isEmpty()) {
            Set<MediaType> mediaTypes = info.getProducesCondition().getProducibleMediaTypes();
            request.setAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);
        }
    }

    private boolean isMatrixVariableContentAvailable() {
        return !getUrlPathHelper().shouldRemoveSemicolonContent();
    }

    private Map<String, MultiValueMap<String, String>> extractMatrixVariables(
            HttpServletRequest request, Map<String, String> uriVariables) {

        Map<String, MultiValueMap<String, String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> uriVar : uriVariables.entrySet()) {
            String uriVarValue = uriVar.getValue();

            int equalsIndex = uriVarValue.indexOf('=');
            if (equalsIndex == -1) {
                continue;
            }

            String matrixVariables;

            int semicolonIndex = uriVarValue.indexOf(';');
            if ((semicolonIndex == -1) || (semicolonIndex == 0) || (equalsIndex < semicolonIndex)) {
                matrixVariables = uriVarValue;
            } else {
                matrixVariables = uriVarValue.substring(semicolonIndex + 1);
                uriVariables.put(uriVar.getKey(), uriVarValue.substring(0, semicolonIndex));
            }

            MultiValueMap<String, String> vars = WebUtils.parseMatrixVariables(matrixVariables);
            result.put(uriVar.getKey(), getUrlPathHelper().decodeMatrixVariables(request, vars));
        }
        return result;
    }


    @Override
    protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> requestMappingInfos,
                                          String lookupPath,
                                          HttpServletRequest request) throws ServletException {

        Set<String> allowedMethods = new LinkedHashSet<>(4);

        Set<RequestMappingInfo> patternMatches = new HashSet<>();
        Set<RequestMappingInfo> patternAndMethodMatches = new HashSet<>();

        for (RequestMappingInfo info : requestMappingInfos) {
            if (info.getPatternsCondition().getMatchingCondition(request) != null) {
                patternMatches.add(info);
                if (info.getMethodsCondition().getMatchingCondition(request) != null) {
                    patternAndMethodMatches.add(info);
                } else {
                    for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                        allowedMethods.add(method.name());
                    }
                }
            }
        }

        if (patternMatches.isEmpty()) {
            return null;
        } else if (patternAndMethodMatches.isEmpty() && !allowedMethods.isEmpty()) {

            try {
                throw new Exception(request.getMethod() + allowedMethods);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        Set<MediaType> consumableMediaTypes;
        Set<MediaType> producibleMediaTypes;
        Set<String> paramConditions;

        if (patternAndMethodMatches.isEmpty()) {
            consumableMediaTypes = getConsumableMediaTypes(request, patternMatches);
            producibleMediaTypes = getProducibleMediaTypes(request, patternMatches);
            paramConditions = getRequestParams(request, patternMatches);
        } else {
            consumableMediaTypes = getConsumableMediaTypes(request, patternAndMethodMatches);
            producibleMediaTypes = getProducibleMediaTypes(request, patternAndMethodMatches);
            paramConditions = getRequestParams(request, patternAndMethodMatches);
        }

        if (!consumableMediaTypes.isEmpty()) {
            MediaType contentType = null;
            if (StringUtils.hasLength(request.getContentType())) {
                try {
                    contentType = MediaType.parseMediaType(request.getContentType());
                } catch (IllegalArgumentException ex) {
//                    throw new HttpMediaTypeNotSupportedException(ex.getMessage());
                    ex.printStackTrace();
                }
            }

            try {
                throw new Exception(contentType.toString() + new ArrayList<>(consumableMediaTypes).size() + "");
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (!producibleMediaTypes.isEmpty()) {

//            throw new HttpMediaTypeNotAcceptableException(new ArrayList<MediaType>(producibleMediaTypes));
            try {
                throw new Exception(new ArrayList<MediaType>(producibleMediaTypes) + "");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (!CollectionUtils.isEmpty(paramConditions)) {
            String[] params = paramConditions.toArray(new String[paramConditions.size()]);

//            throw new UnsatisfiedServletRequestParameterException(params, request.getParameterMap());
            try {
                throw new Exception(params.toString() + request.getParameterMap() + "");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            return null;
        }

        return null;
    }

    private Set<MediaType> getConsumableMediaTypes(HttpServletRequest request, Set<RequestMappingInfo> partialMatches) {
        Set<MediaType> result = new HashSet<>();
        for (RequestMappingInfo partialMatch : partialMatches) {
            if (partialMatch.getConsumesCondition().getMatchingCondition(request) == null) {
                result.addAll(partialMatch.getConsumesCondition().getConsumableMediaTypes());
            }
        }
        return result;
    }

    private Set<MediaType> getProducibleMediaTypes(HttpServletRequest request, Set<RequestMappingInfo> partialMatches) {
        Set<MediaType> result = new HashSet<>();
        for (RequestMappingInfo partialMatch : partialMatches) {
            if (partialMatch.getProducesCondition().getMatchingCondition(request) == null) {
                result.addAll(partialMatch.getProducesCondition().getProducibleMediaTypes());
            }
        }
        return result;
    }

    private Set<String> getRequestParams(HttpServletRequest request, Set<RequestMappingInfo> partialMatches) {

        for (RequestMappingInfo partialMatch : partialMatches) {

            ParamsRequestCondition condition = partialMatch.getParamsCondition();

            if (!CollectionUtils.isEmpty(condition.getExpressions()) && (condition.getMatchingCondition(request) == null)) {
                Set<String> expressions = new HashSet<>();

                for (NameValueExpression<String> expr : condition.getExpressions()) {
                    expressions.add(expr.toString());
                }
                return expressions;
            }

        }

        return null;
    }

}
