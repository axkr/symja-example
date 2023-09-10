package com.symja.programming.document;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.symja.common.logging.DLog;
import com.symja.programming.ProgrammingContract;
import com.symja.programming.R;
import com.symja.programming.document.model.DocumentItem;
import com.symja.programming.document.model.DocumentStructureLoader;
import com.symja.programming.document.view.MarkdownViewDelegate;
import com.symja.programming.document.view.NativeMarkdownView;
import com.symja.programming.view.text.SimpleTextWatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MarkdownListDocumentFragment extends Fragment
        implements MarkdownListDocumentAdapter.OnDocumentClickListener, ProgrammingContract.IDocumentView, MarkdownViewDelegate {

    private static final String EXTRA_QUERY = "MarkdownListDocumentFragment.EXTRA_QUERY";
    private static final String EXTRA_ITEMS = "MarkdownListDocumentFragment.EXTRA_ITEMS";
    private static final String EXTRA_DISPLAYING_ITEM = "MarkdownListDocumentFragment.EXTRA_ITEM_STACK";

    private static final String TAG = "MarkdownListDocumentFra";
    @Nullable
    private EditText searchView;
    @Nullable
    private ViewFlipper viewFlipper;
    private MarkdownListDocumentAdapter adapter;

    private Stack<DocumentItem> displayingItemStack = new Stack<>();

    public static MarkdownListDocumentFragment newInstance(ArrayList<DocumentItem> items) {
        Bundle args = new Bundle();
        MarkdownListDocumentFragment fragment = new MarkdownListDocumentFragment();
        args.putSerializable(EXTRA_ITEMS, items);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_programming_document, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<DocumentItem> documentItems = getDocumentItems();

        //noinspection ConstantConditions
        @NonNull Context context = getContext();

        adapter = new MarkdownListDocumentAdapter(context, documentItems);
        adapter.setOnDocumentClickListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        searchView = view.findViewById(R.id.edit_search_view);
        searchView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                onQueryTextChange(s.toString());
            }
        });

        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(EXTRA_QUERY);
            if (query != null && !query.isEmpty()) {
                searchView.setText(query);
            }
        }
        viewFlipper = view.findViewById(R.id.view_flipper2);
        viewFlipper.setDisplayedChild(0);
        viewFlipper.setInAnimation(getContext(), android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        restoreState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null) {
            outState.putString(EXTRA_QUERY, searchView.getText().toString());
        }
        if (displayingItemStack != null) {
            outState.putSerializable(EXTRA_DISPLAYING_ITEM, new ArrayList<>(displayingItemStack));
        }
    }

    @NonNull
    private ArrayList<DocumentItem> getDocumentItems() {
        ArrayList<DocumentItem> documentItems = null;
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(EXTRA_ITEMS)) {
            try {
                //noinspection unchecked
                List<DocumentItem> serializable = (List<DocumentItem>) arguments.getSerializable(EXTRA_ITEMS);
                if (serializable != null) {
                    documentItems = new ArrayList<>(serializable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (documentItems == null) {
            documentItems = new ArrayList<>();
        }
        return documentItems;
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_DISPLAYING_ITEM)) {
            //noinspection unchecked
            List<DocumentItem> documentItems = (List<DocumentItem>) savedInstanceState.getSerializable(EXTRA_DISPLAYING_ITEM);
            if (documentItems != null) {
                for (DocumentItem documentItem : documentItems) {
                    onDocumentClick(documentItem);
                }
            }
        }
    }

    @Override
    public void onDocumentClick(DocumentItem item) {
        if (viewFlipper == null) {
            return;
        }

        setPushAnimation(viewFlipper);
        if (getActivity() != null) {
            hideKeyboard(getActivity());
        }

        try {
            // stack markdown view
            View view = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_markdown_view, viewFlipper, false);
            viewFlipper.addView(view);
            viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 1);

            NativeMarkdownView markdownView = view.findViewById(R.id.markdown_view);
            markdownView.setDelegate(this);
            markdownView.loadMarkdownFromAssets(item.getAssetPath());

            displayingItemStack.push(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(item.getName());
            }
        }
    }

    @Override
    public void onUrlClick(CharSequence title, String url) {
        if (viewFlipper == null || getContext() == null) {
            return;
        }
        if (getActivity() != null) {
            hideKeyboard(getActivity());
        }

        setPushAnimation(viewFlipper);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_webview, viewFlipper, false);
        viewFlipper.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        WebView webView = view.findViewById(R.id.web_view);
        final ContentLoadingProgressBar progressBar = view.findViewById(R.id.progress_bar);
        viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 1);

        // avoid user goto other pages
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.hide();
            }
        });
        webView.loadUrl(url);

    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private void setPushAnimation(ViewFlipper viewFlipper) {
        viewFlipper.setInAnimation(getContext(), R.anim.slide_in_right);
        viewFlipper.setOutAnimation(getContext(), R.anim.slide_out_left);
    }

    private void setPopAnimation(ViewFlipper viewFlipper) {
        viewFlipper.setInAnimation(getContext(), R.anim.slide_in_left);
        viewFlipper.setOutAnimation(getContext(), R.anim.slide_out_right);
    }

    private void onQueryTextChange(String newText) {
        adapter.query(newText);
    }

    @Override
    public void setPresenter(ProgrammingContract.IPresenter presenter) {
    }

    @Override
    public boolean onBackPressed() {
        if (viewFlipper != null && viewFlipper.getDisplayedChild() > 0) {
            int displayedChild = viewFlipper.getDisplayedChild();
            View container = viewFlipper.getChildAt(displayedChild);
            View markdownView = container.findViewById(R.id.markdown_view);
            if (markdownView != null && !displayingItemStack.empty()) {
                displayingItemStack.pop();
            }
            setPopAnimation(viewFlipper);
            viewFlipper.getOutAnimation().setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (viewFlipper.getChildCount() >= 2) {
                        viewFlipper.removeViewAt(viewFlipper.getChildCount() - 1);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (viewFlipper.getChildCount() >= 2) {
                viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 2);
            }
            return true;
        }
        return false;
    }

    @Override
    public void openDocument(DocumentItem documentItem) {
        onDocumentClick(documentItem);
    }

    @Override
    public void onLinkClick(@Nullable CharSequence title, @NonNull String url) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "onLinkClick() called with: title = [" + title + "], url = [" + url + "]");
        }
        if (getContext() == null) {
            return;
        }
        if (url.startsWith("https://") || url.startsWith("http://")) {
            onUrlClick(title, url);
            return;
        } else if (url.startsWith("#")) { // page navigation
            if (viewFlipper != null) {
                View pageView = viewFlipper.getChildAt(viewFlipper.getChildCount() - 1);
                View sectionView = pageView.findViewWithTag("#" + url.toLowerCase().replaceAll("\\W", ""));
                if (sectionView != null && pageView instanceof ScrollView) {
                    ((ScrollView) pageView).smoothScrollTo(0, sectionView.getTop());
                }
            }
            return;
        }
        ArrayList<DocumentItem> documentItems = DocumentStructureLoader.loadDocumentStructure(getContext());
        for (DocumentItem documentItem : documentItems) {
            String assetPath = documentItem.getAssetPath();
            if (assetPath.contains(url) || url.contains(assetPath)) {
                onDocumentClick(documentItem);
                break;
            }
        }
    }

    @Override
    public void onCopyCodeButtonClicked(@Nullable View v, @NonNull final String code) {
        ExpressionCopyingDialog.show(getContext(), v, code);
    }
}
