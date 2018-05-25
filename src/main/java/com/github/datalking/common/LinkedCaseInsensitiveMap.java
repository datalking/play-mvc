package com.github.datalking.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * key大小写不敏感的 LinkedHashMap
 * 存储时都使用小写
 */
public class LinkedCaseInsensitiveMap<V> extends LinkedHashMap<String, V> {

    private Map<String, String> caseInsensitiveKeys;

    private final Locale locale;

    public LinkedCaseInsensitiveMap() {
        this(null);
    }

    public LinkedCaseInsensitiveMap(Locale locale) {
        super();
        this.caseInsensitiveKeys = new HashMap<>();
        this.locale = (locale != null ? locale : Locale.getDefault());
    }

    public LinkedCaseInsensitiveMap(int initialCapacity) {
        this(initialCapacity, null);
    }

    public LinkedCaseInsensitiveMap(int initialCapacity, Locale locale) {
        super(initialCapacity);
        this.caseInsensitiveKeys = new HashMap<>(initialCapacity);
        this.locale = (locale != null ? locale : Locale.getDefault());
    }


    @Override
    public V put(String key, V value) {
        String oldKey = this.caseInsensitiveKeys.put(convertKey(key), key);
        if (oldKey != null && !oldKey.equals(key)) {
            super.remove(oldKey);
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        if (map.isEmpty()) {
            return;
        }
        for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return (key instanceof String && this.caseInsensitiveKeys.containsKey(convertKey((String) key)));
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            String caseInsensitiveKey = this.caseInsensitiveKeys.get(convertKey((String) key));
            if (caseInsensitiveKey != null) {
                return super.get(caseInsensitiveKey);
            }
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        if (key instanceof String) {
            String caseInsensitiveKey = this.caseInsensitiveKeys.remove(convertKey((String) key));
            if (caseInsensitiveKey != null) {
                return super.remove(caseInsensitiveKey);
            }
        }
        return null;
    }

    @Override
    public void clear() {
        this.caseInsensitiveKeys.clear();
        super.clear();
    }

    @Override
    public Object clone() {
        LinkedCaseInsensitiveMap<V> copy = (LinkedCaseInsensitiveMap<V>) super.clone();
        copy.caseInsensitiveKeys = new HashMap<>(this.caseInsensitiveKeys);
        return copy;
    }

    protected String convertKey(String key) {
        return key.toLowerCase(this.locale);
    }

}
