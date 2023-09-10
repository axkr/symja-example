package com.symja.programming.console;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.symja.programming.console.models.CalculationItem;


public interface OnProgrammingItemClickListener {

    void onRemoveClicked(RecyclerView.ViewHolder holder);

    void onInputViewClicked(View view, @NonNull CalculationItem item);

    void openWebView(@NonNull String html, @NonNull String baseUrl, @NonNull String mimeType);
}