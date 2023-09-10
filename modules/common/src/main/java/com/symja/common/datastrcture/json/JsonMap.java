package com.symja.common.datastrcture.json;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonMap extends HashMap<String, Object> {
    public JsonMap() {

    }

    public JsonMap(Map<String, Object> map) {
        super(map);
    }

    @Nullable
    public String getString(String key) {
        if (!has(key)) {
            return null;
        }
        return String.valueOf(get(key));
    }

    public Integer getInt(String key) {
        return Integer.valueOf(String.valueOf(get(key)));
    }

    public Long getLong(String key) {
        return Long.valueOf(String.valueOf(get(key)));
    }

    @SuppressWarnings("unchecked")
    public JsonMap getJsonMap(String key) {
        Object obj = get(key);
        if (obj instanceof JsonMap) {
            return (JsonMap) obj;
        }
        return new JsonMap((Map<String, Object>) obj);
    }

    public Boolean getBoolean(String key) {
        return Boolean.valueOf(String.valueOf(get(key)));
    }

    public Boolean getBooleanOrDefault(String key, boolean def) {
        try {
            Object o = get(key);
            if (String.valueOf(o).equalsIgnoreCase("true")) {
                return true;
            } else if (String.valueOf(o).equalsIgnoreCase("false")) {
                return false;
            }
            return def;
        } catch (Exception e) {
            return false;
        }
    }

    public List getListInteger(String key) {
        return (List) get(key);
    }

    public List getListObject(String key) {
        Object o = get(key);
        return (List) o;
    }

    public boolean has(String key) {
        return containsKey(key);
    }

    public void checkKeys(String... keys) {
        for (String key : keys) {
            if (!has(key)) {
                throw new WrongJsonFormatException(this);
            }
        }
    }

}
