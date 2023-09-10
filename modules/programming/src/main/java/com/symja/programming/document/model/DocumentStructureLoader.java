package com.symja.programming.document.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.common.datastrcture.json.JSONArray;
import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;
import com.symja.common.logging.DLog;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;

public class DocumentStructureLoader {
    @Nullable
    private static ArrayList<DocumentItem> sAllDocumentItems;
    private static ArrayList<DocumentItem> sTutorialItems;
    private static ArrayList<DocumentItem> sFunctionCatalog;

    public static ArrayList<DocumentItem> loadDocumentStructure(@NonNull Context context) {
        if (sAllDocumentItems != null && !sAllDocumentItems.isEmpty()) {
            // Clone it to avoid modification
            return new ArrayList<>(sAllDocumentItems);
        }
        try {
            InputStream in = context.getAssets().open("doc/help_functions_md_index.json");
            JSONObject root = new JSONObject(org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8));

            JSONArray tutorialJSON = root.getJSONArray("children").getJSONObject(0).getJSONArray("children");
            sTutorialItems = new ArrayList<>();
            loadChildren(tutorialJSON, sTutorialItems, "doc", false);
            Comparator<DocumentItem> comparator = Comparator.comparing(DocumentItem::getName);
            sTutorialItems.sort(comparator);

            sFunctionCatalog = new ArrayList<>();
            for (int i = 0; i < tutorialJSON.length(); i++) {
                JSONObject object = tutorialJSON.getJSONObject(i);
                if (object.getString("name").equals("functions")) {
                    JSONArray functionJSON = object.getJSONArray("children");
                    ArrayList<DocumentItem> functions = new ArrayList<>();
                    loadChildren(functionJSON, functions, "doc/functions", false);
                    functions.sort(comparator);
                    sFunctionCatalog.addAll(functions);
                }
            }

            DocumentStructureLoader.sAllDocumentItems = new ArrayList<>();
            DocumentStructureLoader.sAllDocumentItems.addAll(sTutorialItems);
            DocumentStructureLoader.sAllDocumentItems.addAll(sFunctionCatalog);
        } catch (Exception e) {
            e.printStackTrace();
            if (DLog.DEBUG) {
                throw new RuntimeException(e);
            }
        }
        // Clone it to avoid modification
        return new ArrayList<>(sAllDocumentItems);
    }

    public static ArrayList<DocumentItem> getFunctionCatalog(Context context) {
        if (sFunctionCatalog != null && !sFunctionCatalog.isEmpty()) {
            return new ArrayList<>(sFunctionCatalog);
        }
        loadDocumentStructure(context);
        // Clone it to avoid modification
        return new ArrayList<>(sFunctionCatalog);
    }

    public static ArrayList<DocumentItem> getTutorialItems(Context context) {
        loadDocumentStructure(context);
        // Clone it to avoid modification
        return new ArrayList<>(sTutorialItems);
    }

    /**
     * Structure: {"name": "fileName", "children": [ ... ] }
     */
    private static void loadChildren(JSONArray files, ArrayList<DocumentItem> documentItems,
                                     String parentPath, boolean recursive) throws JSONException {
        for (int i = 0; i < files.length(); i++) {
            JSONObject child = files.getJSONObject(i);
            String fileName = child.getString("name");
            if (fileName.equalsIgnoreCase("index.md")) {
                continue;
            }

            String name = fileName.replace("-", " ");
            name = name.replace(".md", "");
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

            String assetPath = parentPath + "/" + fileName;
            // check is directory
            if (child.has("children")) {
                if (recursive) {
                    loadChildren(child.getJSONArray("children"), documentItems, assetPath, recursive);
                }
            } else {
                String description = null;
                if (child.has("desc")) {
                    description = child.getString("desc");
                }
                documentItems.add(new DocumentItem(assetPath, name, description));
            }
        }
    }

    @Nullable
    public static DocumentItem getFunctionDocument(Context context, String name) {
        ArrayList<DocumentItem> functionCatalog = getFunctionCatalog(context);
        for (DocumentItem documentItem : functionCatalog) {
            if (documentItem.getName().equalsIgnoreCase(name)) {
                return documentItem;
            }
        }
        return null;
    }
}
