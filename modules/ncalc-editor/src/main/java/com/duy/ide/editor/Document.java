/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.editor;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.duy.ide.editor.theme.model.EditorTheme;
import com.duy.ide.editor.view.IEditAreaView;
import com.jecelyin.editor.v2.highlight.TextBuffer;

import org.gjt.sp.jedit.LineManager;
import org.gjt.sp.jedit.syntax.ModeProvider;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class Document implements TextWatcher {
    private final Context mContext;
    private final TextBuffer mBuffer;

    private final HashMap<Integer, ArrayList<? extends CharacterStyle>> mColorSpanMap;

    private final Highlighter mHighlighter;
    private final IEditAreaView mEditText;

    private int mLineCount;
    private String mModeName;

    public Document(@NonNull Context context, @NonNull IEditAreaView editorView) {
        mContext = context;
        mColorSpanMap = new HashMap<>();
        mHighlighter = new Highlighter();

        mBuffer = new TextBuffer();

        mEditText = editorView;
        mLineCount = 1;

        mEditText.addTextChangedListener(this);
        mEditText.setInitLineNumber(1);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            Editable editableText = mEditText.getEditableText();
            mBuffer.setEditable(editableText);

            if (before > 0) {
                mBuffer.remove(start, before);
            }
            if (count > 0) {
                mBuffer.insert(start, s.subSequence(start, start + count));
            }

            mLineCount = mBuffer.getLineManager().getLineCount();


            LineManager lineManager = mBuffer.getLineManager();
            int startLine = lineManager.getLineOfOffset(start);
            int endLine = lineManager.getLineOfOffset(start + count);
            int lineStartOffset = lineManager.getLineStartOffset(startLine);
            int lineEndOffset = lineManager.getLineEndOffset(endLine);

            boolean canHighlight = mBuffer.isCanHighlight();
            if (!canHighlight) {
                return;
            }
            {
                ForegroundColorSpan[] spans = editableText.getSpans(lineStartOffset, lineEndOffset, ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    editableText.removeSpan(span);
                }
            }
            EditorTheme editorTheme = mEditText.getEditorTheme();
            mHighlighter.highlight(mBuffer, editorTheme, mColorSpanMap, editableText, startLine, endLine);
        } catch (Throwable e) {
            //should not happened
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void setMode(String name) {
        mModeName = name;

        mBuffer.setMode(ModeProvider.getInstance().getMode(name), mContext);
//        mEditText.getEditableText().clearSpans();

        highlightSyntax(mEditText);
    }

    public String getModeName() {
        return mModeName;
    }

    public int getLineCount() {
        return mLineCount;
    }

    private void highlightSyntax(IEditAreaView editAreaView) {
        EditorTheme editorTheme = mEditText.getEditorTheme();
        Editable editableText = editAreaView.getEditableText();
        try {
            mHighlighter.highlight(mBuffer, editorTheme, mColorSpanMap, editableText, 0, mLineCount - 1);
        } catch (Exception e) {
            //should not happened
            e.printStackTrace();
        }
    }

    public void highlightWarn(int startLine, int endLine) {
        EditorTheme editorTheme = mEditText.getEditorTheme();
        Editable editableText = mEditText.getEditableText();
        try {
            mHighlighter.highlightWarn(mBuffer, editorTheme, mColorSpanMap, editableText, startLine, endLine, false);
        } catch (Exception e) {
            //should not happened
            e.printStackTrace();
        }
    }

    public void highlightError(int startLine, int endLine) {
        EditorTheme editorTheme = mEditText.getEditorTheme();
        Editable editableText = mEditText.getEditableText();
        try {
            mHighlighter.highlightError(
                    mBuffer,
                    editorTheme,
                    mColorSpanMap,
                    editableText,
                    startLine - 1 /*line start at 0*/,
                    endLine - 1 /*line start at 0*/,
                    false);
        } catch (Exception e) {
            //should not happened
            e.printStackTrace();
        }
    }

    public TextBuffer getBuffer() {
        return mBuffer;
    }


}
