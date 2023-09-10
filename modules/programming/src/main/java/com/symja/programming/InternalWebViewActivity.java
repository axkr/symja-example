package com.symja.programming;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class InternalWebViewActivity extends BaseActivity {

    private static final String EXTRA_HTML = "EXTRA_HTML";
    private static final String EXTRA_BASE_URL = "EXTRA_BASE_URL";
    private static final String EXTRA_MIME_TYPE = "EXTRA_MIME_TYPE";

    public static void open(@NonNull Fragment fragment, String html, String baseUrl, String mimeType) {
        Intent intent = new Intent(fragment.getContext(), InternalWebViewActivity.class);
        putDataIntoIntent(intent, html, baseUrl, mimeType);
        fragment.startActivity(intent);
    }

    public static void open(@NonNull Activity activity, String html, String baseUrl, String mimeType) {
        Intent intent = new Intent(activity, InternalWebViewActivity.class);
        putDataIntoIntent(intent, html, baseUrl, mimeType);
        activity.startActivity(intent);
    }

    private static void putDataIntoIntent(Intent intent, String html, String baseUrl, String mimeType) {
        if (baseUrl == null) {
            baseUrl = "file:///android_asset/";
        }
        if (mimeType == null) {
            mimeType = "text/html; charset=UTF-8";
        }
        if (html != null) {
            intent.putExtra(EXTRA_HTML, html);
        }
        intent.putExtra(EXTRA_BASE_URL, baseUrl);
        intent.putExtra(EXTRA_MIME_TYPE, mimeType);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_webview);

        WebView webView = findViewById(R.id.web_view);
        Intent intent = getIntent();
        String html = intent.getStringExtra(EXTRA_HTML);
        String baseUrl = intent.getStringExtra(EXTRA_BASE_URL);
        String mimeType = intent.getStringExtra(EXTRA_MIME_TYPE);
        webView.loadDataWithBaseURL(baseUrl, html, mimeType, "UTF-8", null);

        setupToolbar();
    }
}
