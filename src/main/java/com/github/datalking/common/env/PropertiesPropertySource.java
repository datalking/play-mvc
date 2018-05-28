package com.github.datalking.common.env;

import java.util.Map;
import java.util.Properties;

/**
 * @author yaoo on 5/28/18
 */
public class PropertiesPropertySource extends MapPropertySource {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PropertiesPropertySource(String name, Properties source) {
        super(name, (Map) source);
    }

}
