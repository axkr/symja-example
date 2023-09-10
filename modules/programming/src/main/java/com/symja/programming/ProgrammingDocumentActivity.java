package com.symja.programming;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.symja.programming.document.MarkdownListDocumentFragment;
import com.symja.programming.document.model.DocumentStructureLoader;
import com.symja.programming.utils.ApplicationUtils;

public class ProgrammingDocumentActivity extends BaseActivity {
    public static final String EXTRA_OPEN_TUTORIALS = "EXTRA_OPEN_TUTORIALS";
    private MarkdownListDocumentFragment documentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programming_document);
        setupToolbar();
        //changeSystemBarColor();
        if (getIntent().hasExtra(EXTRA_OPEN_TUTORIALS)
                && getIntent().getBooleanExtra(EXTRA_OPEN_TUTORIALS, false)) {
            setTitle(R.string.programming_tutorials);
            documentFragment = MarkdownListDocumentFragment.newInstance(
                    DocumentStructureLoader.getTutorialItems(this));
        } else {
            setTitle(R.string.programming_catalog);
            documentFragment = MarkdownListDocumentFragment.newInstance(
                    DocumentStructureLoader.getFunctionCatalog(this));

        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, documentFragment).commitAllowingStateLoss();

        findViewById(R.id.btn_feedback).setOnClickListener(
                v -> ApplicationUtils.feedback(ProgrammingDocumentActivity.this));
    }

    @Override
    public void onBackPressed() {
        if (documentFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}