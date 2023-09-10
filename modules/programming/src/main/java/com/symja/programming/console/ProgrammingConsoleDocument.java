package com.symja.programming.console;

import android.util.Log;

import androidx.annotation.NonNull;

import com.symja.common.datastrcture.json.JSONArray;
import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;
import com.symja.common.datastrcture.json.JsonIO;
import com.symja.common.datastrcture.json.JsonMap;
import com.symja.common.logging.DLog;
import com.symja.programming.console.models.CalculationItem;

import java.util.ArrayList;
import java.util.List;


public class ProgrammingConsoleDocument extends ArrayList<CalculationItem> implements JsonIO {
    private static final String TAG = "ProgrammingConsoleDocum";

    @NonNull
    private final String name;

    public ProgrammingConsoleDocument(@NonNull String name) {
        this.name = name;
    }

    public ProgrammingConsoleDocument(@NonNull JsonMap properties) {
        properties.checkKeys(Key.NAME, Key.DATA);
        //noinspection ConstantConditions
        this.name = properties.getString(Key.NAME);
        List items = properties.getListObject(Key.DATA);
        for (Object json : items) {
            if (json instanceof JsonMap) {
                try {
                    JsonMap entryProperties = (JsonMap) json;
                    this.add(new CalculationItem(entryProperties));
                } catch (Exception e) {
                    if (DLog.DEBUG) {
                        Log.w(TAG, e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void writeToJson(JSONObject out) throws JSONException {
        JSONArray items = new JSONArray();
        for (CalculationItem calculationItem : this) {
            JSONObject properties = new JSONObject();
            calculationItem.writeToJson(properties);
            items.put(properties);
        }
        out.put(Key.NAME, name);
        out.put(Key.DATA, items);
        out.put(Key.VERSION, "1.0");
    }

    @NonNull
    public String getName() {
        return name;
    }

    public static class Key {
        static final String NAME = "name";
        static final String DATA = "data";


        static final String VERSION = "version";

    }
}
