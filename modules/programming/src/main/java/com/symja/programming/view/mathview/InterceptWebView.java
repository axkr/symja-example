package com.symja.programming.view.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.symja.common.logging.DLog;

public class InterceptWebView extends WebView {
    private static final String TAG = "InterceptWebView";

    private WebViewScrollerHelper helper;
    private String data;

    private boolean disableWebRedirect = true;


    public InterceptWebView(Context context) {
        super(context);
        setup(context);
    }

    public InterceptWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public InterceptWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setup(Context context) {
        getSettings().setSupportZoom(false);
        getSettings().setBuiltInZoomControls(false);

        //hide control view
        getSettings().setDisplayZoomControls(false);

        getSettings().setJavaScriptEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        setBackgroundColor(Color.TRANSPARENT);
        helper = new WebViewScrollerHelper(this);

        disableLinkClick();
    }

    public void setDisableWebRedirect(boolean disableWebRedirect) {
        this.disableWebRedirect = disableWebRedirect;
    }

    private void disableLinkClick() {
        // prevent open browser
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (DLog.DEBUG) {
                    Log.d(TAG, "shouldOverrideUrlLoading() called with: view = [" + view + "], url = [" + url + "]");
                }
                return disableWebRedirect;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (DLog.DEBUG) {
                    Log.d(TAG, "shouldOverrideUrlLoading() called with: view = [" + view + "], request = [" + request + "]");
                }
                return disableWebRedirect;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (DLog.DEBUG)
                    Log.d(TAG, "shouldInterceptRequest() called with: view = [" + view + "], request = [" + request + "]");
                return super.shouldInterceptRequest(view, request);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (DLog.DEBUG)
                    Log.d(TAG, "shouldInterceptRequest() called with: view = [" + view + "], url = [" + url + "]");
                return super.shouldInterceptRequest(view, url);
            }
        });
    }

    @Override
    public void loadData(String data, @Nullable String mimeType, @Nullable String encoding) {
        this.data = data;
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(@Nullable String baseUrl, String data, @Nullable String mimeType, @Nullable String encoding, @Nullable String historyUrl) {
        this.data = data;
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (helper.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public String getData() {
        return data;
    }
}
