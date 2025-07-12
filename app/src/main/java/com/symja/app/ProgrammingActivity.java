package com.symja.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;
import com.symja.common.analyst.AppAnalytics;
import com.symja.common.analyst.AppAnalyticsEvents;
import com.symja.programming.BaseActivity;
import com.symja.programming.ProgrammingPresenter;
import com.symja.programming.console.ProgrammingConsoleFragment;
import com.symja.programming.document.MarkdownListDocumentFragment;
import com.symja.programming.document.model.DocumentItem;
import com.symja.programming.document.model.DocumentStructureLoader;
import com.symja.programming.symjatalk.SymjaTalkFragment;
import com.symja.programming.view.ActivityConstants;

import java.util.ArrayList;


public class ProgrammingActivity extends BaseActivity {
    public static final String EXTRA_INPUT = "com.duy.calc.casio.programming.ProgrammingActivity.EXTRA_INPUT";
    private static final String EXTRA_PAGE_INDEX = "ProgrammingActivity.EXTRA_PAGE_INDEX";

    private ViewFlipper viewFlipper;
    private TabLayout tabLayout;
    private ProgrammingPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symja_prgm_activity_programming);
        setupEdgeToEdge();

        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(com.symja.programming.R.string.symja_prgm_title_programming);
        changeSystemBarColor();

        viewFlipper = findViewById(R.id.view_flipper);
        tabLayout = findViewById(R.id.tab_layout);

        ProgrammingConsoleFragment programmingConsoleFragment = setupProgrammingTab();
        SymjaTalkFragment symjaTalkFragment = setupSymjaTalkTab();
        MarkdownListDocumentFragment documentFragment = setupDocumentTab();

        presenter = new ProgrammingPresenter(viewFlipper, tabLayout);
        presenter.setConsoleView(programmingConsoleFragment);
        presenter.setSymjaTalkView(symjaTalkFragment);
        presenter.setDocumentView(documentFragment);

        setupTabView();

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_PAGE_INDEX)) {
            int selectedTabPosition = savedInstanceState.getInt(EXTRA_PAGE_INDEX);
            if (selectedTabPosition >= 0 && selectedTabPosition < tabLayout.getTabCount()) {
                tabLayout.selectTab(tabLayout.getTabAt(selectedTabPosition));
            }
        }
    }

    private void setupEdgeToEdge() {
        ViewGroup rootView = findViewById(R.id.root_view);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            rootView.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupTabView() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        AppAnalytics.getInstance(ProgrammingActivity.this)
                                .logEvent(AppAnalyticsEvents.PROGRAMMING_OPEN_CONSOLE, new Bundle());
                        viewFlipper.setDisplayedChild(0);
                        break;
                    case 1:
                        AppAnalytics.getInstance(ProgrammingActivity.this)
                                .logEvent(AppAnalyticsEvents.PROGRAMMING_OPEN_SYMJATALK, new Bundle());
                        viewFlipper.setDisplayedChild(1);
                        break;
                    case 2:
                        AppAnalytics.getInstance(ProgrammingActivity.this)
                                .logEvent(AppAnalyticsEvents.PROGRAMMING_OPEN_DOCUMENT, new Bundle());
                        viewFlipper.setDisplayedChild(2);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @NonNull
    private SymjaTalkFragment setupSymjaTalkTab() {
        SymjaTalkFragment symjaTalkFragment = (SymjaTalkFragment) getSupportFragmentManager().findFragmentByTag("SymjaTalkFragment");
        if (symjaTalkFragment == null) {
            symjaTalkFragment = SymjaTalkFragment.newInstance(null);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_programming_pods, symjaTalkFragment, "SymjaTalkFragment")
                .commitAllowingStateLoss();
        return symjaTalkFragment;
    }

    @NonNull
    private ProgrammingConsoleFragment setupProgrammingTab() {


        ProgrammingConsoleFragment programmingConsoleFragment = (ProgrammingConsoleFragment) getSupportFragmentManager().findFragmentByTag(ProgrammingConsoleFragment.TAG);
        if (programmingConsoleFragment == null) {
            programmingConsoleFragment = ProgrammingConsoleFragment.newInstance(getInput());
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_programming_console, programmingConsoleFragment, ProgrammingConsoleFragment.TAG)
                .commitAllowingStateLoss();
        return programmingConsoleFragment;
    }

    @NonNull
    private MarkdownListDocumentFragment setupDocumentTab() {
        MarkdownListDocumentFragment documentFragment = (MarkdownListDocumentFragment) getSupportFragmentManager().findFragmentByTag("DocumentFragment");
        if (documentFragment == null) {
            ArrayList<DocumentItem> tutorialItems = DocumentStructureLoader.getTutorialItems(this);
            ArrayList<DocumentItem> functionCatalog = DocumentStructureLoader.getFunctionCatalog(this);
            ArrayList<DocumentItem> combined = new ArrayList<>(tutorialItems.size() + functionCatalog.size());
            combined.addAll(tutorialItems);
            combined.addAll(functionCatalog);
            documentFragment = MarkdownListDocumentFragment.Companion.newInstance(combined);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_programming_document, documentFragment, "DocumentFragment")
                .commitAllowingStateLoss();

        return documentFragment;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_PAGE_INDEX, tabLayout.getSelectedTabPosition());
    }

    @Override
    public void onBackPressed() {
        if (!presenter.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Nullable
    private String getInput() {
        String input = null;
        Intent intent = getIntent();
        if (intent != null) {
            input = intent.getStringExtra(EXTRA_INPUT);
        }
        return input;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ActivityConstants.RESULT_NEED_RESTART) {
            restart();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
