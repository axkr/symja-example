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

package com.duy.ide.editor.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

import com.duy.editor.R;
import com.duy.ide.editor.Document;

public class CodeEditor extends EditActionSupportEditor {
    private Document mDocument;

    public CodeEditor(Context context) {
        super(context);
        init(null);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mDocument = new Document(getContext(), this);
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CodeEditor);
            String language = ta.getString(R.styleable.CodeEditor_language);
            ta.recycle();
            if (language != null && !language.isEmpty()) {
                mDocument.setMode(language);
            }
        }

        setTokenizer(new SymbolTokenizer());
    }

    public Document getDocument() {
        return mDocument;
    }


    /**
     * This simple Tokenizer can be used for lists where the items are
     * separated by a comma and one or more spaces.
     */
    public static class SymbolTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        private static final String SYMBOL = "";

        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && Character.isLetterOrDigit(text.charAt(i - 1))) {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (!Character.isLetterOrDigit(text.charAt(i))) {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
//            int i = text.length();
//
//            while (i > 0 && text.charAt(i - 1) == ' ') {
//                i--;
//            }
            return text;

//            if (i > 0 && Character.isLetterOrDigit(text.charAt(i - 1))) {
//                return text;
//            } else {
//                if (text instanceof Spanned) {
//                    SpannableString sp = new SpannableString(text );
//                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
//                            Object.class, sp, 0);
//                    return sp;
//                } else {
//                    return text ;
//                }
//            }
        }
    }
}
