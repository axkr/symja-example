package com.symja.programming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.symja.common.analyst.AppAnalytics;
import com.symja.common.analyst.AppAnalyticsEvents;
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
    private TabLayout navigationView;
    private ProgrammingPresenter presenter;

    /**
     * Open programming editor with given input
     */
    public static void edit(String input, Context context) {
        AppAnalytics.getInstance(context)
                .logEvent(AppAnalyticsEvents.OPEN_PROGRAMMING, new Bundle());
        Intent intent = new Intent(context, ProgrammingActivity.class);
        intent.putExtra(EXTRA_INPUT, input);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 0);
        } else {
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programming2);
        setupToolbar();
        setTitle(R.string.programming_title);
        changeSystemBarColor();

        viewFlipper = findViewById(R.id.view_flipper);

        ProgrammingConsoleFragment programmingConsoleFragment = (ProgrammingConsoleFragment) getSupportFragmentManager().findFragmentByTag(ProgrammingConsoleFragment.TAG);
        if (programmingConsoleFragment == null) {
            programmingConsoleFragment = ProgrammingConsoleFragment.newInstance(getInput());
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_programming_console, programmingConsoleFragment, ProgrammingConsoleFragment.TAG)
                .commitAllowingStateLoss();


        SymjaTalkFragment symjaTalkFragment = (SymjaTalkFragment) getSupportFragmentManager().findFragmentByTag("SymjaTalkFragment");
        if (symjaTalkFragment == null) {
            symjaTalkFragment = SymjaTalkFragment.newInstance(null);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_programming_pods, symjaTalkFragment, "SymjaTalkFragment")
                .commitAllowingStateLoss();


        MarkdownListDocumentFragment documentFragment = (MarkdownListDocumentFragment) getSupportFragmentManager().findFragmentByTag("DocumentFragment");
        if (documentFragment == null) {
            ArrayList<DocumentItem> tutorialItems = DocumentStructureLoader.getTutorialItems(this);
            ArrayList<DocumentItem> functionCatalog = DocumentStructureLoader.getFunctionCatalog(this);
            ArrayList<DocumentItem> combined = new ArrayList<>(tutorialItems.size() + functionCatalog.size());
            combined.addAll(tutorialItems);
            combined.addAll(functionCatalog);
            documentFragment = MarkdownListDocumentFragment.newInstance(combined);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_programming_document, documentFragment, "DocumentFragment")
                .commitAllowingStateLoss();

        navigationView = findViewById(R.id.tab_layout);
        presenter = new ProgrammingPresenter(viewFlipper, navigationView);
        presenter.setConsoleView(programmingConsoleFragment);
        presenter.setDocumentView(documentFragment);
        // TODO presenter.setCatalogView(catalogFragment);

        navigationView.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        viewFlipper.setDisplayedChild(0);
                        setTitle(R.string.programming_console);
                        //noinspection ConstantConditions
                        getSupportActionBar().setSubtitle(null);
                        break;
                    case 1:
                        AppAnalytics.getInstance(ProgrammingActivity.this)
                                .logEvent(AppAnalyticsEvents.PROGRAMMING_OPEN_DOCUMENT, new Bundle());
                        viewFlipper.setDisplayedChild(1);
                        setTitle(R.string.programming_document);
                        //noinspection ConstantConditions
                        getSupportActionBar().setSubtitle(null);
                        break;
                    case 2:
                        AppAnalytics.getInstance(ProgrammingActivity.this)
                                .logEvent(AppAnalyticsEvents.PROGRAMMING_OPEN_CATALOG, new Bundle());
                        viewFlipper.setDisplayedChild(2);
                        setTitle(R.string.programming_catalog);
                        //noinspection ConstantConditions
                        getSupportActionBar().setSubtitle(null);
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

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_PAGE_INDEX)) {
            int selectedTabPosition = savedInstanceState.getInt(EXTRA_PAGE_INDEX);
            if (selectedTabPosition >= 0 && selectedTabPosition < navigationView.getTabCount()) {
                navigationView.selectTab(navigationView.getTabAt(selectedTabPosition));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_PAGE_INDEX, navigationView.getSelectedTabPosition());
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
