package com.symja.programming.document.view;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface MarkdownViewDelegate {
    void onLinkClick(@Nullable CharSequence title, @NonNull String url);

    void onCopyCodeButtonClicked(@Nullable View v, @NonNull String code);
}
