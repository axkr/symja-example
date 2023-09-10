package com.symja.programming.view.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.symja.common.logging.DLog;

public class InternalWebView extends WebView {

    private static final String TAG = "InternalWebView";

    private boolean preventOpenBrowser = true;

    public InternalWebView(Context context) {
        super(context);
        setup(context);
    }

    public InternalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public InternalWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setup(Context context) {
        if (isInEditMode()) {
            return;
        }
        WebSettings settings = getSettings();
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        //hide control view
        settings.setDisplayZoomControls(false);

        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        // prevent open browser
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (preventOpenBrowser) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (preventOpenBrowser) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                if (DLog.DEBUG)
                    Log.w(TAG, message.messageLevel().name() + ": " + message.message());
                return false;
            }
        });
    }

    public void setPreventOpenBrowser(boolean preventOpenBrowser) {
        this.preventOpenBrowser = preventOpenBrowser;
    }

    public void loadHtml(String html) {
        loadData(html, "text/html", null);
    }
}
