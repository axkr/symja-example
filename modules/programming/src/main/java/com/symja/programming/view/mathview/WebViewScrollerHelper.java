package com.symja.programming.view.mathview;

import android.view.MotionEvent;
import android.webkit.WebView;

public class WebViewScrollerHelper {
    private static final String TAG = "WebViewScrollerHelper";

    private WebView webView;

    public WebViewScrollerHelper(WebView webView) {
        this.webView = webView;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Negative to check scrolling left, positive to check scrolling right.
                if (/*webView.canScrollHorizontally(1)
                        || webView.canScrollHorizontally(-1)
                        || webView.canScrollVertically(1)
                        || webView.canScrollVertically(1)*/ true) {
                        webView.getParent().requestDisallowInterceptTouchEvent(true);
                } else if (event.getPointerCount() >= 2) {
                    webView.getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                webView.getParent().requestDisallowInterceptTouchEvent(false);
                break;

            case MotionEvent.ACTION_MOVE:
                break;
        }
        return false;
    }

}
