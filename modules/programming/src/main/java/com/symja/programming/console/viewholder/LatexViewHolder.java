package com.symja.programming.console.viewholder;

import android.view.View;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;

import com.duy.ide.editor.view.CodeEditor;
import com.google.android.material.tabs.TabLayout;
import com.symja.common.datastrcture.Data;
import com.symja.programming.R;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.view.NativeLatexView;

import java.util.List;

public class LatexViewHolder extends BaseViewHolder {

    private final CodeEditor inputTextView;
    private final NativeLatexView inputLatexView;
    private final NativeLatexView resultLatexView;

    private final CodeEditor resultPlainTextView;

    private final TabLayout resultTabLayout;
    private final ViewFlipper resultViewFlipper;

    public LatexViewHolder(@NonNull View itemView) {
        super(itemView);
        inputTextView = itemView.findViewById(R.id.input_text);

        inputLatexView = itemView.findViewById(R.id.input_latex_view);
        resultLatexView = itemView.findViewById(R.id.result_latex_view);

        resultPlainTextView = itemView.findViewById(R.id.result_plaintext_view);

        resultViewFlipper = itemView.findViewById(R.id.result_view_flipper);
        resultTabLayout = itemView.findViewById(R.id.result_tab_layout);
        resultTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                resultViewFlipper.setDisplayedChild(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void bindData(@NonNull CalculationItem item, OnProgrammingItemClickListener onProgrammingItemClickListener) {
        super.bindData(item, onProgrammingItemClickListener);

        inputLatexView.setVisibility(View.GONE);
        inputTextView.setVisibility(View.GONE);

        resultPlainTextView.setText("");

        Data input = item.getInput();
        if (input.getFormat() == Data.Format.TEXT_APPLICATION_SYMJA
                || input.getFormat() == Data.Format.TEXT_PLAIN) {
            inputTextView.setVisibility(View.VISIBLE);
            inputTextView.setText(input.getValue());
        }
        if (input.getFormat() == Data.Format.LATEX) {
            inputLatexView.setVisibility(View.VISIBLE);
            inputLatexView.setLatex(input.getValue());
        }

        List<Data> results = item.getDataList();
        for (Data result : results) {
            if (result.getFormat() == Data.Format.LATEX) {
                if (result.isEmpty()) {
                    resultLatexView.setVisibility(View.GONE);

                } else {
                    resultLatexView.setVisibility(View.VISIBLE);
                    String latex = result.getValue();
                    resultLatexView.setLatex(latex);
                }
            }

            if (result.getFormat() == Data.Format.TEXT_PLAIN
                    || result.getFormat() == Data.Format.TEXT_APPLICATION_SYMJA) {
                resultPlainTextView.setText(result.getValue());
            }
        }
    }
}
