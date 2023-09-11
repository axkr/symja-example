package com.symja.programming.document.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class DocumentItem implements Serializable {
    @NonNull
    private String assetPath;
    @NonNull
    private String name;
    @Nullable
    private String description;

    public DocumentItem(@NonNull String assetPath, @NonNull String name, @Nullable String description) {
        this.assetPath = assetPath;
        this.name = name;
        this.description = description;
    }

    @NonNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItem that = (DocumentItem) o;
        return assetPath.equals(that.assetPath) && name.equals(that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetPath, name, description);
    }
}
