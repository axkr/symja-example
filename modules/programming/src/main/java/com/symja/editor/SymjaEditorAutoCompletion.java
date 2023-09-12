package com.symja.editor;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;

public class SymjaEditorAutoCompletion extends EditorAutoCompletion {
    /**
     * Create a panel instance for the given editor
     *
     * @param editor Target editor
     */
    public SymjaEditorAutoCompletion(@NonNull CodeEditor editor) {
        super(editor);
    }

    @Override
    public boolean isFeatureEnabled(int feature) {
        if (feature == FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED) {
            return true;
        }
        return super.isFeatureEnabled(feature);
    }
}
