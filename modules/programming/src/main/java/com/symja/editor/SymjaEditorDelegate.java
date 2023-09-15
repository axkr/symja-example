package com.symja.editor;

import io.github.rosemoe.sora.lang.completion.CompletionItem;

public interface SymjaEditorDelegate {
    void onSuggestionIconClicked(int position, CompletionItem completionItem);
}
