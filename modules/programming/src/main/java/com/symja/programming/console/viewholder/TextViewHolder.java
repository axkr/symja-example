package com.symja.programming.console.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.duy.ide.editor.view.CodeEditor;
import com.symja.programming.R;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;

public class TextViewHolder extends BaseViewHolder {
    private final CodeEditor txtResult;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
        txtResult = itemView.findViewById(R.id.txt_result);
    }

    @Override
    public void bindData(@NonNull CalculationItem item, OnProgrammingItemClickListener onProgrammingItemClickListener) {
        super.bindData(item, onProgrammingItemClickListener);
        switch (item.getType()) {
            case TEXT_APPLICATION_JAVASCRIPT:
                txtResult.getDocument().setMode("javascript");
                break;
            case TEXT_APPLICATION_SYMJA:
            case TEXT_PLAIN:
                txtResult.getDocument().setMode("symja");
                break;
            case TEXT_APPLICATION_JAVA:
                txtResult.getDocument().setMode("java");
                break;
            case TEXT_MATHML:
                txtResult.getDocument().setMode("xml");
                break;
            case TEXT_HTML:
                txtResult.getDocument().setMode("html");
                break;
            case TEXT_LATEX:
                txtResult.getDocument().setMode("latex");
                break;
            case TEXT_APPLICATION_JSON:
                txtResult.getDocument().setMode("json");
                break;
        }
        txtResult.setText(item.getData());
    }
}
