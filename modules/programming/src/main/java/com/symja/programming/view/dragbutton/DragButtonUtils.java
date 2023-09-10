package com.symja.programming.view.dragbutton;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import java.util.HashMap;

public final class DragButtonUtils {

    private DragButtonUtils() {
    }

    public static float distance(@NonNull PointF start, @NonNull PointF end) {
        return norm(end.x - start.x, end.y - start.y);
    }

    @NonNull
    public static PointF subtract(@NonNull PointF p1, @NonNull PointF p2) {
        return new PointF(p1.x - p2.x, p1.y - p2.y);
    }

    @NonNull
    public static PointF sum(@NonNull PointF p1, @NonNull PointF p2) {
        return new PointF(p1.x + p2.x, p1.y + p2.y);
    }

    private static float norm(float x, float y) {
        return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * https://stackoverflow.com/questions/14066933/direct-way-of-computing-clockwise-angle-between-2-vectors
     */
    public static double getAngleClockwise(PointF start, PointF end) {
        double dot = start.x * end.x + start.y * end.y;
        double det = start.x * end.y - start.y * end.x;
        return Math.atan2(det, dot);
    }

    public static HashMap<DragDirection, String> create(String center, String topLeft, String topRight, String leftBottom, String rightBottom) {
        HashMap<DragDirection, String> map = new HashMap<>();
        map.put(DragDirection.CENTER, center);
        map.put(DragDirection.TOP_LEFT, topLeft);
        map.put(DragDirection.TOP_RIGHT, topRight);
        map.put(DragDirection.LEFT_BOTTOM, leftBottom);
        map.put(DragDirection.RIGHT_BOTTOM, rightBottom);
        return map;
    }
}




