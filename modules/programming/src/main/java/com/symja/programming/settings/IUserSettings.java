package com.symja.programming.settings;

public interface IUserSettings {
    void put(String key, String value);

    void putString(String key, String value);

    String getString(String key, String value);

}
