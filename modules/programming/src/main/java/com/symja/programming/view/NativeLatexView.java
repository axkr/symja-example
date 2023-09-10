package com.symja.programming.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;


import com.symja.common.datastrcture.IExportable;
import com.symja.common.datastrcture.Data;
import com.symja.programming.R;
import com.symja.programming.utils.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import ru.noties.jlatexmath.JLatexMathDrawable;

public class NativeLatexView extends View implements IExportable {

    public static final int ALIGN_START = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_END = 2;
    private int textSize;
    private int textColor;
    @Nullable
    private Drawable background;
    private int alignVertical;
    private int alignHorizontal;
    @Nullable
    private JLatexMathDrawable drawable;
    private float scale;
    private float left;
    private float top;

    @Nullable
    private String latex;

    public NativeLatexView(Context context) {
        super(context);
        init(context, null);
    }

    public NativeLatexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private static float alignment(int align, float difference) {
        final float out;
        if (ALIGN_START == align) {
            out = .0F;
        } else if (ALIGN_CENTER == align) {
            out = difference / 2;
        } else {
            out = difference;
        }
        return out;
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NativeLatexView);
        try {
            setTextSize(ta.getDimensionPixelSize(R.styleable.NativeLatexView_android_textSize,
                    ViewUtils.spToPx(context, 16)));
            textColor(ta.getColor(R.styleable.NativeLatexView_android_textColor,
                    ViewUtils.getColor(context, android.R.attr.textColorPrimary)));
            align(ta.getInteger(R.styleable.NativeLatexView_alignVertical, ALIGN_START),
                    ta.getInteger(R.styleable.NativeLatexView_alignHorizontal, ALIGN_START));

            String latex = ta.getString(R.styleable.NativeLatexView_latex);
            if (latex != null && !latex.isEmpty()) {
                setLatex(latex);
            }
        } finally {
            ta.recycle();
        }
    }

    @NonNull
    public NativeLatexView setTextSize(@Px int textSize) {
        this.textSize = textSize;
        if (latex != null) {
            setLatex(latex);
        }
        requestLayout();
        return this;
    }

    @NonNull
    public NativeLatexView textColor(@Px int textColor) {
        this.textColor = textColor;
        if (latex != null) {
            setLatex(latex);
        }
        postInvalidate();
        return this;
    }

    @NonNull
    public NativeLatexView background(@Nullable Drawable background) {
        this.background = background;
        postInvalidate();
        return this;
    }

    @NonNull
    public NativeLatexView align(@Align int alignVertical, @Align int alignHorizontal) {
        this.alignVertical = alignVertical;
        this.alignHorizontal = alignHorizontal;
        requestLayout();
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setLatex(@NonNull String latex) {
        try {
            String preprocessedLatex = preprocessLatex(latex);
            final JLatexMathDrawable drawable = JLatexMathDrawable.builder(preprocessedLatex)
                    .textSize(textSize)
                    .color(textColor)
                    .background(background)
                    .build();
            setLatexDrawable(drawable);

            this.latex = latex;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            setLatexDrawable(null);
            return false;
        }
    }

    @NotNull
    private String preprocessLatex(@NonNull String latex) {
        return latex.replaceAll("[\r\n]+", " ");
    }

    public void setLatexDrawable(@Nullable JLatexMathDrawable drawable) {
        this.drawable = drawable;
        requestLayout();
    }

    public void clear() {
        this.drawable = null;
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (drawable == null) {
            return;
        }

        final int save = canvas.save();
        try {

            if (left > .0F) {
                canvas.translate(left, 0);
            }
            if (top > .0F) {
                canvas.translate(0, top);
            }

            if (scale > .0F && Float.compare(scale, 1.F) != 0) {
                canvas.scale(scale, scale);
            }

            drawable.draw(canvas);

        } finally {
            canvas.restoreToCount(save);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (drawable == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        // okay, a dimension:
        //  if it's exact -> use it
        //  if it's not -> use smallest value of AT_MOST, (drawable.intrinsic + padding)

        int width;
        int height;

        if (MeasureSpec.EXACTLY == widthMode) {
            width = widthSize;
        } else {
            width = drawableWidth + paddingLeft + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }

        if (MeasureSpec.EXACTLY == heightMode) {
            height = heightSize;
        } else {
            height = drawableHeight + paddingTop + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        final int canvasWidth = width - paddingLeft - getPaddingRight();
        final int canvasHeight = height - paddingTop - getPaddingBottom();

        final float scale;

        // now, let's see if these dimensions change our drawable scale (we need modify it to fit)
        if (drawableWidth < canvasWidth && drawableHeight < canvasHeight) {
            // we do not need to modify drawable
            scale = 1.F;
        } else {
            scale = Math.min(
                    (float) canvasWidth / drawableWidth,
                    (float) canvasHeight / drawableHeight
            );
        }

        final int displayWidth = (int) (drawableWidth * scale + .5F);
        final int displayHeight = (int) (drawableHeight * scale + .5F);

        if (MeasureSpec.EXACTLY != widthMode) {
            width = displayWidth + paddingLeft + getPaddingRight();
        }

        if (MeasureSpec.EXACTLY != heightMode) {
            height = displayHeight + paddingTop + getPaddingBottom();
        }

        // let's see if we should align our formula
        final float left = alignment(alignHorizontal, (width - paddingLeft - getPaddingRight()) - displayWidth);
        final float top = alignment(alignVertical, (height - paddingTop - getPaddingBottom()) - displayHeight);

        this.scale = scale;
        this.left = paddingLeft + left;
        this.top = paddingTop + top;

        setMeasuredDimension(width, height);
    }

    @Nullable
    public JLatexMathDrawable getDrawable() {
        return drawable;
    }

    public String getText() {
        return latex;
    }

    public void setText(String latex) {
        setLatex(latex);
    }

    public int getTextColor() {
        return textColor;
    }

    @Override
    public List<Data.Format> getExportFormats() {
        return Collections.singletonList(
                Data.Format.LATEX);
    }

    @Nullable
    @Override
    public Data exportNow(Data.Format format) {
        if (format == Data.Format.LATEX) {
            return new Data(Data.Format.LATEX, latex);
        }
        return null;
    }

    @Override
    public void export(Data.Format format, Consumer<Data> callback) {
        callback.accept(new Data(Data.Format.LATEX, latex));
    }

    @IntDef({ALIGN_START, ALIGN_CENTER, ALIGN_END})
    @Retention(RetentionPolicy.CLASS)
    public @interface Align {
    }
}
