package com.symja.programming.console;

import android.content.Context;


import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ProgrammingDocumentManager {

    public File directory;

    public ProgrammingDocumentManager(Context context) {
        this.directory = new File(context.getFilesDir(), "programming/documents/");
        this.directory.mkdirs();
    }

    /**
     * Create document with random name
     */
    public ProgrammingConsoleDocument createNewDocument() {
        int count = 0;
        while (true) {
            String newName = "Document" + count + ".json";
            File file = new File(directory, newName);
            if (file.exists()) {
                count++;
                continue;
            }
            return new ProgrammingConsoleDocument(newName);
        }
    }

    public ProgrammingConsoleDocument createDocument(String name) throws IOException {
        File file = new File(directory, name);
        if (file.exists()) {
            throw new IOException("File exist");
        }
        return new ProgrammingConsoleDocument(name);
    }

    public ProgrammingConsoleDocument createIfNotExist(String name) throws IOException {
        return new ProgrammingConsoleDocument(name);
    }

    public ArrayList<String> getAllDocuments() {
        ArrayList<String> names = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.length() != 0) {
                    names.add(file.getName());
                }
            }
        }
        return names;
    }

    public void saveDocument(ProgrammingConsoleDocument document) throws JSONException, IOException {
        File file = new File(directory, document.getName());
        JSONObject jsonObject = new JSONObject();
        document.writeToJson(jsonObject);
        FileUtils.write(file, jsonObject.toString(2), StandardCharsets.UTF_8);
    }

    public ProgrammingConsoleDocument readDocument(String name) throws IOException, JSONException {
        File file = new File(directory, name);
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(s);
        return new ProgrammingConsoleDocument(jsonObject.toMap());
    }

    public boolean deleteDocument(ProgrammingConsoleDocument document) {
        File file = new File(directory, document.getName());
        return file.delete();
    }

    public boolean isExist(ProgrammingConsoleDocument document) {
        return new File(directory, document.getName()).exists();
    }
}
