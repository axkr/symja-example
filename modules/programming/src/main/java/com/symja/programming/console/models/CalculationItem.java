package com.symja.programming.console.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.symja.common.datastrcture.json.JSONArray;
import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;
import com.symja.common.datastrcture.json.JsonIO;
import com.symja.common.datastrcture.json.JsonMap;
import com.symja.common.datastrcture.Data;

import java.util.ArrayList;
import java.util.List;

public class CalculationItem implements JsonIO {

    private static final String VERSION = "1.6";
    /**
     * The original input from user
     */
    @NonNull
    private final Data input;
    /**
     * Result in Symja expression form
     */
    @Nullable
    private final String symjaResult;

    @NonNull
    private final List<Data> dataList;

    @Nullable
    private String title;

    @Nullable
    private String stdErr;
    @Nullable
    private String stdOut;


    public CalculationItem(@Nullable String input,
                           @Nullable String symjaResult,
                           @NonNull Data.Format type,
                           @NonNull String data) {
        this.input = new Data(Data.Format.TEXT_PLAIN, input);
        this.symjaResult = symjaResult;

        this.dataList = new ArrayList<>();
        this.dataList.add(new Data(type, data));
    }

    public CalculationItem(@Nullable Data input,
                           @Nullable String symjaResult,
                           @NonNull List<Data> dataList) {
        this.input = input != null ? input : new Data(Data.Format.NULL, "");
        this.symjaResult = symjaResult;
        this.dataList = dataList;
    }

    public CalculationItem(@NonNull JsonMap properties) {
        properties.checkKeys(Key.DATA);

        this.title = properties.getString(Key.TITLE);
        if (properties.has(Key.INFIX)) {
            this.input = new Data(properties.getJsonMap(Key.INPUT));
        } else {
            this.input = new Data(Data.Format.NULL, "");
        }
        this.symjaResult = properties.getString(Key.INFIX);

        this.dataList = new ArrayList<>();
        List<?> array = properties.getListObject(Key.DATA);
        for (Object o : array) {
            dataList.add(new Data((JsonMap) o));
        }

        if (properties.has(Key.STDERR)) {
            this.stdErr = properties.getString(Key.STDERR);
        }
        if (properties.has(Key.STDOUT)) {
            this.stdOut = properties.getString(Key.STDOUT);
        }
    }

    @Override
    public void writeToJson(JSONObject properties) throws JSONException {
        properties.put(Key.TITLE, getTitle());

        JSONObject inputObj = new JSONObject();
        input.writeToJson(inputObj);
        properties.put(Key.INPUT, inputObj);

        if (getSymjaResult() != null) {
            properties.put(Key.INFIX, getSymjaResult());
        }
        if (getStdErr() != null) {
            properties.put(Key.STDERR, getStdErr());
        }
        if (getStdOut() != null) {
            properties.put(Key.STDOUT, getStdOut());
        }

        JSONArray dataArray = new JSONArray();
        for (Data data : dataList) {
            JSONObject obj = new JSONObject();
            dataArray.put(obj);
            data.writeToJson(obj);
        }
        properties.put(Key.DATA, dataArray);

        properties.put(Key.VERSION, VERSION);
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @NonNull
    public Data getInput() {
        return input;
    }

    @Nullable
    public String getSymjaResult() {
        return symjaResult;
    }

    @Nullable
    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(@Nullable String stdErr) {
        this.stdErr = stdErr;
    }

    @NonNull
    public String getData() {
        if (dataList.size() > 0) {
            Data data = dataList.get(0);
            return data.getValue();
        }
        return "";
    }

    @NonNull
    public Data getData(Data.Format format) {
        for (Data data : dataList) {
            if (data.getFormat() == format) {
                return data;
            }
        }
        return new Data(Data.Format.NULL, "");
    }

    @NonNull
    public Data.Format getType() {
        if (dataList.size() > 0) {
            Data data = dataList.get(0);
            return data.getFormat();
        }
        return Data.Format.TEXT_PLAIN;
    }

    public boolean hasFormat(Data.Format format) {
        return dataList.stream().anyMatch(x -> x.getFormat() == format);
    }

    @NonNull
    public List<Data> getDataList() {
        return dataList;
    }

    @Override
    public String toString() {
        return "CalculationItem{" +
                "input=" + input +
                ", symjaResult='" + symjaResult + '\'' +
                ", title='" + title + '\'' +
                ", error='" + stdErr + '\'' +
                ", dataList=" + dataList +
                '}';
    }

    @Nullable
    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(@Nullable String stdOut) {
        this.stdOut = stdOut;
    }

    public void addData(Data data) {
        this.dataList.add(data);
    }

    static class Key {
        static final String TITLE = "title";
        static final String INPUT = "input";
        static final String INFIX = "infix";
        static final String STDERR = "stderr";
        static final String STDOUT = "stdout";
        static final String DATA = "data";
        static final String VERSION = "version";
    }


}
