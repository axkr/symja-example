package com.symja.programming.view.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.symja.programming.R;

public class BoundedLinearLayout extends LinearLayout {
    private int mMaxWidth = -1;
    private int mMaxHeight = -1;

    public BoundedLinearLayout(Context context) {
        super(context);
        setup(context, null);
    }

    public BoundedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.symja_prgm_BoundedLinearLayout);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.symja_prgm_BoundedLinearLayout_boundedWidth, -1);
            mMaxHeight = a.getDimensionPixelSize(R.styleable.symja_prgm_BoundedLinearLayout_boundedHeight, -1);
            a.recycle();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        }
        if (mMaxWidth > 0) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }
}