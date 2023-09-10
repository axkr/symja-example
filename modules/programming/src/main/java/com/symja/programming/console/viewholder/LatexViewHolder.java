package com.symja.programming.console.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.duy.ide.editor.view.CodeEditor;
import com.symja.common.datastrcture.Data;
import com.symja.programming.R;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.view.NativeLatexView;

public class LatexViewHolder extends BaseViewHolder {

    private final CodeEditor inputText;
    private final NativeLatexView inputLatexView;
    private final NativeLatexView resultView;

    public LatexViewHolder(@NonNull View itemView) {
        super(itemView);
        resultView = itemView.findViewById(R.id.latex_view);
        inputLatexView = itemView.findViewById(R.id.input_latex_view);
        inputText = itemView.findViewById(R.id.input_text);
    }

    @Override
    public void bindData(@NonNull CalculationItem item, OnProgrammingItemClickListener onProgrammingItemClickListener) {
        super.bindData(item, onProgrammingItemClickListener);

        inputLatexView.setVisibility(View.GONE);
        inputText.setVisibility(View.GONE);

        Data input = item.getInput();
        if (input.getFormat() == Data.Format.TEXT_APPLICATION_SYMJA
                || input.getFormat() == Data.Format.TEXT_PLAIN) {
            inputText.setVisibility(View.VISIBLE);
            inputText.setText(input.getValue());
        }
        if (input.getFormat() == Data.Format.LATEX) {
            inputLatexView.setVisibility(View.VISIBLE);
            inputLatexView.setLatex(input.getValue());
        }

        Data data = item.getData(Data.Format.LATEX);

        if (data.isEmpty()) {
            resultView.setVisibility(View.GONE);

        } else {
            resultView.setVisibility(View.VISIBLE);
            String latex = data.getValue();
            resultView.setLatex(latex);
        }
    }
}
