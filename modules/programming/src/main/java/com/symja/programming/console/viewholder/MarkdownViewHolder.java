package com.symja.programming.console.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.common.utils.StoreUtil;
import com.symja.programming.R;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.document.ExpressionCopyingDialog;
import com.symja.programming.document.view.MarkdownViewDelegate;
import com.symja.programming.document.view.NativeMarkdownView;

public class MarkdownViewHolder extends BaseViewHolder implements MarkdownViewDelegate {
    private final NativeMarkdownView markdownView;

    public MarkdownViewHolder(@NonNull View itemView) {
        super(itemView);
        markdownView = itemView.findViewById(R.id.markdown_view);
        markdownView.setDelegate(this);
    }

    @Override
    public void bindData(@NonNull CalculationItem item, OnProgrammingItemClickListener onProgrammingItemClickListener) {
        super.bindData(item, onProgrammingItemClickListener);
        markdownView.loadMarkdown(item.getData());
    }

    @Override
    public void onLinkClick(@Nullable CharSequence title, @NonNull String url) {
        StoreUtil.openBrowser(itemView.getContext(), url);
    }

    @Override
    public void onCopyCodeButtonClicked(@Nullable View v, @NonNull String code) {
        ExpressionCopyingDialog.show(itemView.getContext(), v, code);
    }
}
