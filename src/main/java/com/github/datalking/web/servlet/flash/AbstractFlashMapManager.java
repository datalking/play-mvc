package com.github.datalking.web.servlet.flash;

import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.Assert;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.web.servlet.FlashMapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yaoo on 4/28/18
 */
public abstract class AbstractFlashMapManager implements FlashMapManager {

    private static final Object writeLock = new Object();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int flashMapTimeout = 180;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    public void setFlashMapTimeout(int flashMapTimeout) {
        this.flashMapTimeout = flashMapTimeout;
    }


    public int getFlashMapTimeout() {
        return this.flashMapTimeout;
    }


    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    public UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }

    @Override
    public final FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response) {
        List<FlashMap> maps = retrieveFlashMaps(request);
        if (CollectionUtils.isEmpty(maps)) {
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved FlashMap(s): " + maps);
        }
        List<FlashMap> mapsToRemove = getExpiredFlashMaps(maps);

        FlashMap match = getMatchingFlashMap(maps, request);
        if (match != null) {
            mapsToRemove.add(match);
        }

        if (!mapsToRemove.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Removing FlashMap(s): " + mapsToRemove);
            }
            synchronized (writeLock) {
                maps = retrieveFlashMaps(request);
                maps.removeAll(mapsToRemove);
                updateFlashMaps(maps, request, response);
            }
        }

        return match;
    }

    @Override
    public final void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(flashMap)) {
            return;
        }

        String path = decodeAndNormalizePath(flashMap.getTargetRequestPath(), request);
        flashMap.setTargetRequestPath(path);
        decodeParameters(flashMap.getTargetRequestParams(), request);

        if (logger.isDebugEnabled()) {
            logger.debug("Saving FlashMap=" + flashMap);
        }
        flashMap.startExpirationPeriod(this.flashMapTimeout);

        synchronized (writeLock) {
            List<FlashMap> allMaps = retrieveFlashMaps(request);
            allMaps = (allMaps != null ? allMaps : new CopyOnWriteArrayList<>());
            allMaps.add(flashMap);
            updateFlashMaps(allMaps, request, response);
        }
    }

    protected abstract List<FlashMap> retrieveFlashMaps(HttpServletRequest request);

    private List<FlashMap> getExpiredFlashMaps(List<FlashMap> allMaps) {
        List<FlashMap> result = new ArrayList<>();
        for (FlashMap map : allMaps) {
            if (map.isExpired()) {
                result.add(map);
            }
        }
        return result;
    }


    private FlashMap getMatchingFlashMap(List<FlashMap> allMaps, HttpServletRequest request) {
        List<FlashMap> result = new ArrayList<>();
        for (FlashMap flashMap : allMaps) {
            if (isFlashMapForRequest(flashMap, request)) {
                result.add(flashMap);
            }
        }
        if (!result.isEmpty()) {
            Collections.sort(result);
            if (logger.isDebugEnabled()) {
                logger.debug("Found matching FlashMap(s): " + result);
            }
            return result.get(0);
        }
        return null;
    }

    protected boolean isFlashMapForRequest(FlashMap flashMap, HttpServletRequest request) {
        String expectedPath = flashMap.getTargetRequestPath();
        if (expectedPath != null) {
            String requestUri = this.urlPathHelper.getOriginatingRequestUri(request);
            if (!requestUri.equals(expectedPath) && !requestUri.equals(expectedPath + "/")) {
                return false;
            }
        }
        MultiValueMap<String, String> targetParams = flashMap.getTargetRequestParams();
        for (String expectedName : targetParams.keySet()) {
            for (String expectedValue : targetParams.get(expectedName)) {
                if (!ObjectUtils.containsElement(request.getParameterValues(expectedName), expectedValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String decodeAndNormalizePath(String path, HttpServletRequest request) {
        if (path != null) {
            path = this.urlPathHelper.decodeRequestString(request, path);
            if (path.charAt(0) != '/') {
                String requestUri = this.urlPathHelper.getRequestUri(request);
                path = requestUri.substring(0, requestUri.lastIndexOf('/') + 1) + path;
                path = StringUtils.cleanPath(path);
            }
        }
        return path;
    }

    private void decodeParameters(MultiValueMap<String, String> params, HttpServletRequest request) {
        for (String name : new ArrayList<>(params.keySet())) {
            for (String value : new ArrayList<>(params.remove(name))) {
                name = this.urlPathHelper.decodeRequestString(request, name);
                value = this.urlPathHelper.decodeRequestString(request, value);
                params.add(name, value);
            }
        }
    }

    protected abstract void updateFlashMaps(List<FlashMap> flashMaps, HttpServletRequest request, HttpServletResponse response);

}
