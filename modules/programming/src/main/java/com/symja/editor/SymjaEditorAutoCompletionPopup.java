package com.symja.editor;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;

public class SymjaEditorAutoCompletionPopup extends EditorAutoCompletion {
    private SymjaCompletionAdapter symjaCompletionAdapter;

    // setting this property allow to expand the completion popup large than view height
    private int overrideMaxHeight = 0;

    /**
     * Create a panel instance for the given editor
     *
     * @param editor Target editor
     */
    public SymjaEditorAutoCompletionPopup(@NonNull CodeEditor editor) {
        super(editor);
        SymjaCompletionLayout layout = new SymjaCompletionLayout();
        setLayout(layout);
        symjaCompletionAdapter = new SymjaCompletionAdapter();
        setAdapter(symjaCompletionAdapter);
    }

    @Override
    public boolean isFeatureEnabled(int feature) {
        if (feature == FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED) {
            return true;
        }
        return super.isFeatureEnabled(feature);
    }

    @Override
    public void setMaxHeight(int height) {
        super.setMaxHeight(Math.max(height, overrideMaxHeight));
    }

    public SymjaCompletionAdapter getSymjaCompletionAdapter() {
        return symjaCompletionAdapter;
    }

    public void setOverrideMaxHeight(int overrideMaxHeight) {
        this.overrideMaxHeight = overrideMaxHeight;
    }

    public void setSymjaCompletionAdapter(SymjaCompletionAdapter symjaCompletionAdapter) {
        this.symjaCompletionAdapter = symjaCompletionAdapter;
        setAdapter(symjaCompletionAdapter);
    }
}
