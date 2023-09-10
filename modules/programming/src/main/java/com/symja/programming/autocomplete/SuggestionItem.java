package com.symja.programming.autocomplete;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class SuggestionItem implements Serializable {
    @Nullable
    private final String assetPath;
    @NonNull
    private final String name;
    @Nullable
    private final String description;

    public SuggestionItem(@Nullable String assetPath, @NonNull String name, @Nullable String description) {
        this.assetPath = assetPath;
        this.name = name;
        this.description = description;
    }

    @Nullable
    public String getAssetPath() {
        return assetPath;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Override
    @NonNull
    public String toString() {
        return "FunctionDocumentItem{" +
                "assetPath='" + assetPath + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
