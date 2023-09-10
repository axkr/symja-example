package com.symja.programming.console.viewholder;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.programming.R;
import com.symja.common.logging.DLog;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.utils.ViewUtils;
import com.symja.programming.view.mathview.InterceptWebView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class WebViewHolder extends BaseViewHolder {
    private static final String TAG = "WebViewHolder";

    private final InterceptWebView webView;
    private final View btnExpand;

    public WebViewHolder(@NonNull View itemView) {
        super(itemView);
        webView = itemView.findViewById(R.id.web_view);
        btnExpand = itemView.findViewById(R.id.btn_expand);
    }

    @Nullable
    public static String buildHtml(@NonNull Context context, @NonNull CalculationItem item) {
        if (item.getData().isEmpty()) {
            return null;
        }
        try {
            switch (item.getType()) {
                case IFRAME: {
                    String pageContent = IOUtils.toString(context.getAssets()
                                    .open("iframe_template.html"))
                            .replace("\r", "");
                    String html = pageContent.replaceAll("`1`", item.getData());
                    if (DLog.DEBUG) {
                        try {
                            File htmlFile = new File(context.getCacheDir(), "html/" + System.currentTimeMillis() + ".html");
                            FileUtils.write(htmlFile, html, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            DLog.e(TAG, e);
                        }
                    }
                    return html;
                }
                case HTML:
                case LATEX:
                    return item.getData();
            }
        } catch (Exception e) {
            if (DLog.DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }
        return null;
    }

    @Override
    public void bindData(@NonNull CalculationItem item,
                         final OnProgrammingItemClickListener onProgrammingItemClickListener) {
        super.bindData(item, onProgrammingItemClickListener);

        Context context = webView.getContext();

        if (item.getData().isEmpty()) {
            webView.setVisibility(View.GONE);
        } else {
            webView.setVisibility(View.VISIBLE);
            final String html = buildHtml(context, item);
            final String baseUrl = "file:///android_asset/";
            final String mimeType = "text/html; charset=UTF-8";

            webView.getSettings().setSupportZoom(false);
            webView.loadDataWithBaseURL(baseUrl, html, mimeType, null, null);
            webView.setScrollbarFadingEnabled(false);
            webView.setScrollBarSize(ViewUtils.dpToPx(context, 2));
            webView.setVerticalScrollBarEnabled(true);

            if (btnExpand != null) {
                btnExpand.setVisibility(View.VISIBLE);
                btnExpand.setOnClickListener(v -> onProgrammingItemClickListener.openWebView(html, baseUrl, mimeType));
            }
        }
    }
}
