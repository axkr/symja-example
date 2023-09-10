package com.symja.editor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.langs.textmate.TextMateAnalyzer;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

public class SymjaLanguageProxy implements Language {
    private final TextMateLanguage textMateLanguage;
    private final SymjaAutoCompleteProvider autoCompleteProvider;
    private boolean autoCompleteEnable = true;

    public SymjaLanguageProxy(TextMateLanguage mathematicaLanguage) {
        this.textMateLanguage = mathematicaLanguage;
        this.autoCompleteProvider = new SymjaAutoCompleteProvider();
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
        return autoCompleteEnable;
    }

    public void setAutoCompleteEnable(boolean autoCompleteEnable) {
        this.autoCompleteEnable = autoCompleteEnable;
    }

    @Override
    public void requireAutoComplete(@NonNull ContentReference content, @NonNull CharPosition position, @NonNull CompletionPublisher publisher, @NonNull Bundle extraArguments) {
        if (!isAutoCompleteEnabled()) {
            return;
        }
        String prefix = CompletionHelper.computePrefix(content, position, MyCharacter::isJavaIdentifierPart);
        TextMateAnalyzer textMateAnalyzer = (TextMateAnalyzer) textMateLanguage.getAnalyzeManager();
        IdentifierAutoComplete.SyncIdentifiers idt;
        try {
            Field field = TextMateAnalyzer.class.getDeclaredField("syncIdentifiers");
            field.setAccessible(true);
            idt = (IdentifierAutoComplete.SyncIdentifiers) field.get(textMateAnalyzer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        autoCompleteProvider.requireAutoComplete(content, position, prefix, publisher, idt);
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
