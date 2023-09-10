package com.symja.programming.document.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FunctionDocumentItem extends DocumentItem {

    public FunctionDocumentItem(@NonNull String assetPath, @NonNull String name, @Nullable String description) {
        super(assetPath, name, description);
    }
}
