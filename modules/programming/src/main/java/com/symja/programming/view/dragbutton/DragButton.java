package com.symja.programming.view.dragbutton;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
import static android.view.HapticFeedbackConstants.KEYBOARD_TAP;

import static com.symja.programming.view.dragbutton.DragButtonUtils.subtract;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.common.logging.DLog;
import com.symja.programming.R;
import com.symja.programming.utils.ViewUtils;

import java.util.HashMap;
import java.util.Map;

public class DragButton extends FrameLayout implements DragView {
    private static final String TAG = "DragButton";

    @Nullable
    private DragListener listener;
    private boolean vibrateOnDrag = true;

    private HashMap<DragDirection, TextView> viewMap = new HashMap<>();
    private HashMap<DragDirection, String> directionTextMap;

    // 100 ms
    @SuppressWarnings("FieldCanBeLocal")
    private int clickInterval = 100;
    @Nullable
    private PointF startPoint;
    private long startTime;

    public DragButton(Context context) {
        super(context);
        setupView(context, null);
    }

    public DragButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView(context, attrs);
    }

    public DragButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupView(context, attrs);
    }

    private void setupView(Context context, AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.symja_prgm_direction_button_content, this);
        disableAllChildren(this);

        viewMap.put(DragDirection.CENTER, (TextView) findViewById(R.id.txt_center));
        viewMap.put(DragDirection.TOP_LEFT, (TextView) findViewById(R.id.txt_top_left));
        viewMap.put(DragDirection.TOP_RIGHT, (TextView) findViewById(R.id.txt_top_right));
        viewMap.put(DragDirection.RIGHT_BOTTOM, (TextView) findViewById(R.id.txt_right_bottom));
        viewMap.put(DragDirection.LEFT_BOTTOM, (TextView) findViewById(R.id.txt_left_bottom));

        try {
            @SuppressLint("CustomViewStyleable")
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.symja_prgm_DragButton);
            if (typedArray.hasValue(R.styleable.symja_prgm_DragButton_textSize)) {
                int textSize = typedArray.getDimensionPixelSize(R.styleable.symja_prgm_DragButton_textSize, 14);
                for (TextView textView : viewMap.values()) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                }
            }
            typedArray.recycle();
        } catch (Exception e){
            DLog.w(TAG, e);
        }

    }

    private void disableAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                disableAllChildren((ViewGroup) child);
            } else {
                child.setClickable(false);
                child.setFocusable(false);
                child.setFocusableInTouchMode(false);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTracking(event);
                handleTouchEvent(event, State.STARTED);
                return true;

            case MotionEvent.ACTION_MOVE:
                handleTouchEvent(event, State.DRAGGING);
                return true;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleTouchEvent(event, State.STOPPED);
                stopTracking(event);
                return true;
        }
        return super.onTouchEvent(event);
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean handleTouchEvent(MotionEvent e, State state) {
        if (startPoint == null) {
            return false;
        }
        final long duration = System.currentTimeMillis() - startTime;
        DragDirection direction;
        if (duration < clickInterval) {
            DLog.d(TAG, "onDrag: center");
            direction = DragDirection.CENTER;
        } else {
            PointF end = new PointF(e.getX(), e.getY());
            final float distance = DragButtonUtils.distance(startPoint, end);
            float minDragDistanceInPixels = getWidth() / 3.0f;
            if (distance < minDragDistanceInPixels) {
                DLog.d(TAG, "onDrag: center");
                direction = DragDirection.CENTER;
            } else {
                PointF axis = new PointF(1, 0);

                double pi2 = Math.PI * 2;
                double radians = (DragButtonUtils.getAngleClockwise(axis, subtract(end, startPoint)) + pi2) % pi2;
                if (DLog.DEBUG) {
                    DLog.d(TAG, "radians = " + radians + " angle = " + Math.toDegrees(radians));
                }
                direction = DragDirection.getDirection(radians);
            }
        }
        if (DLog.DEBUG) {
            DLog.d(TAG, "direction = " + direction);
        }

        setSelectedDirection(direction);
        if (listener != null) {
            listener.onDrag(this, direction, directionTextMap.get(direction), state);
            if (vibrateOnDrag && state == State.STOPPED) {
                performHapticFeedback(KEYBOARD_TAP,
                        FLAG_IGNORE_GLOBAL_SETTING | FLAG_IGNORE_VIEW_SETTING);
            }
        }
        return listener != null;
    }

    public void setSelectedDirection(DragDirection direction) {
        int defaultTextColor = ViewUtils.getColor(getContext(), android.R.attr.textColorPrimary);
        int accentTextColor = ViewUtils.getColor(getContext(), android.R.attr.colorAccent);
        for (Map.Entry<DragDirection, TextView> pair : viewMap.entrySet()) {
            if (pair.getKey() == direction) {
                if (pair.getValue() != null) {
                    pair.getValue().setTextColor(accentTextColor);
                }
            } else {
                if (pair.getValue() != null) {
                    pair.getValue().setTextColor(defaultTextColor);
                }
            }
        }

    }

    private void startTracking(@NonNull MotionEvent event) {
        startPoint = new PointF(event.getX(), event.getY());
        startTime = System.currentTimeMillis();
    }

    private void stopTracking(MotionEvent event) {
        int defaultTextColor = ViewUtils.getColor(getContext(), android.R.attr.textColorPrimary);
        startPoint = null;
        for (TextView value : viewMap.values()) {
            if (value != null) {
                value.setTextColor(defaultTextColor);
            }
        }
    }

    @Override
    public void setOnDragListener(@Nullable DragListener listener) {
        this.listener = listener;
    }

    public void setVibrateOnDrag(boolean vibrateOnDrag) {
        this.vibrateOnDrag = vibrateOnDrag;
    }

    @Override
    public HashMap<DragDirection, String> getDirectionTextMap() {
        return directionTextMap;
    }

    @Override
    public void setDirectionTextMap(HashMap<DragDirection, String> directionTextMap) {
        this.directionTextMap = directionTextMap;
        for (Map.Entry<DragDirection, String> pair : directionTextMap.entrySet()) {
            TextView view = viewMap.get(pair.getKey());
            if (view != null) {
                view.setText(pair.getValue());
            }
        }
    }

    public enum State {
        STARTED, DRAGGING, STOPPED
    }

}
