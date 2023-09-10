package com.symja.programming.document.view.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

public class RoundedCornerSpan extends ReplacementSpan {

    private int textColor;
    private int cornerRadius;
    private int backgroundColor;
    private boolean includePadding = false;

    public RoundedCornerSpan(int backgroundColor, int textColor, int cornerRadius) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.cornerRadius = cornerRadius;
    }

    public void setIncludePadding(boolean includePadding) {
        this.includePadding = includePadding;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (includePadding ? cornerRadius * 2 : 0) + Math.round(measureText(paint, text, start, end));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        int padding = includePadding ? cornerRadius : 0;
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        RectF rect = new RectF(x, y + fontMetrics.ascent,
                x + measureText(paint, text, start, end) + padding * 2,
                y + fontMetrics.descent);

        paint.setColor(backgroundColor);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        paint.setColor(textColor);
        canvas.drawText(text, start, end, x + padding, y, paint);
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}