package com.symja.programming.database;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class JsonDatabase<E> implements IDatabase<E> {
    private static final String KEY_ARRAY = "array";
    private final File storedFile;

    public JsonDatabase(File storedFile) {
        this.storedFile = storedFile;
    }

    private JSONObject getDatabase() {
        try {
            FileInputStream input = new FileInputStream(storedFile);
            String content = IOUtils.toString(input, StandardCharsets.UTF_8);
            input.close();
            return new JSONObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void save(List<E> database) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        jsonObject.put(KEY_ARRAY, array);
        for (E e : database) {
            JSONObject item = new JSONObject();
            writeToJson(e, item);
            array.put(item);
        }
        write(jsonObject);
    }


    private void write(JSONObject database) {
        try {
            FileOutputStream output = new FileOutputStream(storedFile);
            output.write(database.toString().getBytes());
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<E> getAll() {
        ArrayList<E> list = new ArrayList<>();
        try {
            JSONObject database = getDatabase();
            JSONArray array = database.getJSONArray(KEY_ARRAY);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                E item = readFromJson(jsonObject);
                list.add(item);
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    protected abstract void writeToJson(E e, JSONObject out) throws JSONException;

    protected abstract E readFromJson(JSONObject in) throws JSONException;

    @Override
    public void insert(E item) throws JSONException {
        List<E> database = getAll();
        database.add(item);
        save(database);
    }

    @Override
    public void update(E newValue) throws JSONException {
        List<E> data = getAll();
        for (int i = 0; i < data.size(); i++) {
            E element = data.get(i);
            if (element.equals(newValue)) {
                data.set(i, newValue);
            }
        }
        save(data);
    }

    @Override
    public void delete(E toBeRemove) throws JSONException {
        List<E> data = getAll();
        for (Iterator<E> iterator = data.iterator(); iterator.hasNext(); ) {
            E e = iterator.next();
            if (e.equals(toBeRemove)) {
                iterator.remove();
            }
        }
        save(data);
    }

    @Override
    public void clear() throws JSONException {
        save(new ArrayList<E>());
    }
}
