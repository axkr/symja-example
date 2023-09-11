package com.symja.programming.document;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.symja.programming.BaseActivity;
import com.symja.programming.R;
import com.symja.programming.document.model.DocumentItem;

import org.jetbrains.annotations.NotNull;


public class MarkdownDocumentActivity extends BaseActivity {

    public static final String EXTRA_ASSET_PATH = "MarkdownDocumentActivity.EXTRA_ASSET_PATH";
    public static final String EXTRA_DOCUMENT_NAME = "MarkdownDocumentActivity.EXTRA_DOCUMENT_NAME";

    public static void open(@NotNull Fragment fragment, @NotNull DocumentItem item) {
        Intent intent = new Intent(fragment.getContext(), MarkdownDocumentActivity.class);
        intent.putExtra(MarkdownDocumentActivity.EXTRA_ASSET_PATH, item.getAssetPath());
        intent.putExtra(MarkdownDocumentActivity.EXTRA_DOCUMENT_NAME, item.getName());
        fragment.startActivityForResult(intent, 0);
    }

    public static void open(Activity fragment, @NotNull DocumentItem item) {
        Intent intent = new Intent(fragment, MarkdownDocumentActivity.class);
        intent.putExtra(MarkdownDocumentActivity.EXTRA_ASSET_PATH, item.getAssetPath());
        intent.putExtra(MarkdownDocumentActivity.EXTRA_DOCUMENT_NAME, item.getName());
        fragment.startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.programming_markdown_document_activity);
        setupToolbar();
        changeSystemBarColor();

        String path = getIntent().getStringExtra(EXTRA_ASSET_PATH);
        if (path == null) {
            return;
        }
        String title = getIntent().getStringExtra(EXTRA_DOCUMENT_NAME);
        setTitle(title);

        MarkdownDocumentFragment fragment = MarkdownDocumentFragment.newInstance(path);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment).commitAllowingStateLoss();
    }
}
