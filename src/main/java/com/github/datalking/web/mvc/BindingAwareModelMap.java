package com.github.datalking.web.mvc;

import com.github.datalking.common.BindingResult;

import java.util.Map;

/**
 * @author yaoo on 4/29/18
 */
public class BindingAwareModelMap extends ExtendedModelMap {

    @Override
    public Object put(String key, Object value) {
        removeBindingResultIfNecessary(key, value);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        for (Map.Entry entry : map.entrySet()) {
            removeBindingResultIfNecessary(entry.getKey(), entry.getValue());
        }
        super.putAll(map);
    }

    private void removeBindingResultIfNecessary(Object key, Object value) {
        if (key instanceof String) {
            String attributeName = (String) key;
            if (!attributeName.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
                String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + attributeName;
                BindingResult bindingResult = (BindingResult) get(bindingResultKey);
                if (bindingResult != null && bindingResult.getTarget() != value) {
                    remove(bindingResultKey);
                }
            }
        }
    }

}
