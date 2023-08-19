package com.symja.app.editor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

public class TextMateLanguageProxy implements Language {
    private final TextMateLanguage textMateLanguage;

    public TextMateLanguageProxy(TextMateLanguage textMateLanguage) {
        this.textMateLanguage = textMateLanguage;
    }


    @NonNull
    @Override
    public AnalyzeManager getAnalyzeManager() {
        return textMateLanguage.getAnalyzeManager();
    }

    @Override
    public int getInterruptionLevel() {
        return textMateLanguage.getInterruptionLevel();
    }

    @Override
    public void destroy() {
        textMateLanguage.destroy();
    }


    @Override
    public boolean useTab() {
        return textMateLanguage.useTab();
    }

    @NonNull
    @Override
    public Formatter getFormatter() {
        return textMateLanguage.getFormatter();
    }

    @Override
    public SymbolPairMatch getSymbolPairs() {
        return textMateLanguage.getSymbolPairs();
    }

    @Override
    public NewlineHandler[] getNewlineHandlers() {
        return textMateLanguage.getNewlineHandlers();
    }

    @Nullable
    @Override
    public QuickQuoteHandler getQuickQuoteHandler() {
        return textMateLanguage.getQuickQuoteHandler();
    }

    public boolean isAutoCompleteEnabled() {
        return textMateLanguage.isAutoCompleteEnabled();
    }

    @Override
    public void requireAutoComplete(@NonNull ContentReference content, @NonNull CharPosition position, @NonNull CompletionPublisher publisher, @NonNull Bundle extraArguments) {
        if (!isAutoCompleteEnabled()) {
            return;
        }
        textMateLanguage.requireAutoComplete(content, position, publisher, extraArguments);
        publisher.setComparator((o1, o2) -> {
            if (o1.label.length() != o2.label.length()) {
                return Integer.compare(o1.label.length(), o2.label.length());
            }
            return o1.label.toString().compareTo(o2.label.toString());
        });
    }

    @Override
    public int getIndentAdvance(@NonNull ContentReference content, int line, int column) {
        return textMateLanguage.getIndentAdvance(content, line, column);
    }

    public TextMateLanguage getTextMateLanguage() {
        return textMateLanguage;
    }

    public IdentifierAutoComplete getAutoCompleter() {
        return textMateLanguage.getAutoCompleter();
    }

    public void setCompleterKeywords(String[] keywords) {
        textMateLanguage.setCompleterKeywords(keywords);
    }
}
