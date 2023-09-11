package com.symja.programming.document.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.symja.common.logging.DLog;
import com.symja.programming.document.view.ext.TeXExtension;

import org.apache.commons.io.IOUtils;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NativeMarkdownView extends LinearLayout {
    private static final String TAG = "NativeMarkdownView";
    private MarkdownViewDelegate delegate;

    public NativeMarkdownView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public NativeMarkdownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public NativeMarkdownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public void setDelegate(MarkdownViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void loadMarkdownFromAssets(@NonNull String assetPath) throws IOException {
        if (DLog.DEBUG) {
            DLog.d(TAG, "loadMarkdownFromAssets() called with: assetPath = [" + assetPath + "]");
        }
        String markdown = IOUtils.toString(getContext().getAssets().open(assetPath), StandardCharsets.UTF_8);
        loadMarkdown(markdown);
    }

    public void loadMarkdown(@NonNull String markdown) {
        try {
            if (DLog.DEBUG) DLog.d(TAG, "markdown = " + markdown);
            final Parser parser = createParser();
            final Node node = parser.parse(markdown);

            removeAllViews();
            render(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public Parser createParser() {
        return new Parser.Builder()
                .extensions(Arrays.asList(
                        TeXExtension.create(),
                        TablesExtension.create()
                ))
                .build();
    }

    private void render(@NotNull Node node) {
        MarkdownVisitor visitor = new MarkdownVisitor(this, delegate);
        node.accept(visitor);
    }

}
