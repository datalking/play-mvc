package com.github.datalking.common.env;

import com.github.datalking.util.StringUtils;

import java.util.Map;

/**
 * 使用map存储键值对
 *
 * @author yaoo on 5/28/18
 */
public class MapPropertySource extends EnumerablePropertySource<Map<String, Object>> {

    public MapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.get(name);
    }

    @Override
    public String[] getPropertyNames() {
        return StringUtils.toStringArray(this.source.keySet());
    }

}
