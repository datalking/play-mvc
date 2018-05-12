package com.github.datalking.web.servlet.flash;

import com.github.datalking.common.LinkedMultiValueMap;
import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.StringUtils;

import java.util.HashMap;

/**
 * @author yaoo on 4/28/18
 */
public class FlashMap extends HashMap<String, Object> implements Comparable<FlashMap> {

    private String targetRequestPath;

    private final MultiValueMap<String, String> targetRequestParams = new LinkedMultiValueMap<>();

    private long expirationStartTime;

    private int timeToLive;

    public void setTargetRequestPath(String path) {
        this.targetRequestPath = path;
    }


    public String getTargetRequestPath() {
        return this.targetRequestPath;
    }


    public FlashMap addTargetRequestParams(MultiValueMap<String, String> params) {
        if (params != null) {
            for (String key : params.keySet()) {
                for (String value : params.get(key)) {
                    addTargetRequestParam(key, value);
                }
            }
        }
        return this;
    }

    public FlashMap addTargetRequestParam(String name, String value) {
        if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
            this.targetRequestParams.add(name, value);
        }
        return this;
    }

    public MultiValueMap<String, String> getTargetRequestParams() {
        return this.targetRequestParams;
    }

    public void startExpirationPeriod(int timeToLive) {
        this.expirationStartTime = System.currentTimeMillis();
        this.timeToLive = timeToLive;
    }

    public boolean isExpired() {
        return (this.expirationStartTime != 0 &&
                (System.currentTimeMillis() - this.expirationStartTime > this.timeToLive * 1000));
    }

    public int compareTo(FlashMap other) {
        int thisUrlPath = (this.targetRequestPath != null ? 1 : 0);
        int otherUrlPath = (other.targetRequestPath != null ? 1 : 0);
        if (thisUrlPath != otherUrlPath) {
            return otherUrlPath - thisUrlPath;
        } else {
            return other.targetRequestParams.size() - this.targetRequestParams.size();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FlashMap [attributes=").append(super.toString());
        sb.append(", targetRequestPath=").append(this.targetRequestPath);
        sb.append(", targetRequestParams=").append(this.targetRequestParams).append("]");
        return sb.toString();
    }

}
