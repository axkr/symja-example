package com.symja.programming.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.symja.common.utils.StoreUtil;
import com.symja.programming.R;
import com.symja.programming.document.view.MarkdownViewDelegate;
import com.symja.programming.document.view.NativeMarkdownView;

import java.io.IOException;


public class MarkdownDocumentFragment extends Fragment implements MarkdownViewDelegate {

    public static final String TAG = "MarkdownDocumentFragment";
    private static final String KEY_ASSET_PATHS = "MarkdownListDocumentFragment.KEY_ASSET_PATH";

    public static MarkdownDocumentFragment newInstance(String assetPath) {

        Bundle args = new Bundle();
        args.putString(KEY_ASSET_PATHS, assetPath);
        MarkdownDocumentFragment fragment = new MarkdownDocumentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NativeMarkdownView documentView = view.findViewById(R.id.markdown_view);
        documentView.setDelegate(this);
        Bundle arguments = getArguments();
        if (arguments != null) {
            try {
                String documentPath = arguments.getString(KEY_ASSET_PATHS);
                if (documentPath != null) {
                    documentView.loadMarkdownFromAssets(documentPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLinkClick(@Nullable CharSequence title, @NonNull String url) {
        StoreUtil.openBrowser(getContext(), url);
    }

    @Override
    public void onCopyCodeButtonClicked(@Nullable View v, @NonNull String code) {
        ExpressionCopyingDialog.show(getContext(), v, code);
    }
}
