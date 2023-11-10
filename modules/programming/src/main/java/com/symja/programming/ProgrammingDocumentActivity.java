package com.symja.programming;

import android.os.Bundle;

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
        setContentView(R.layout.symja_prgm_activity_programming_document);
        setupToolbar();
        //changeSystemBarColor();
        if (getIntent().hasExtra(EXTRA_OPEN_TUTORIALS)
                && getIntent().getBooleanExtra(EXTRA_OPEN_TUTORIALS, false)) {
            setTitle(R.string.symja_prgm_tab_title_tutorials);
            documentFragment = MarkdownListDocumentFragment.Companion.newInstance(
                    DocumentStructureLoader.getTutorialItems(this));
        } else {
            setTitle(R.string.symja_prgm_tab_title_catalog);
            documentFragment = MarkdownListDocumentFragment.Companion.newInstance(
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
