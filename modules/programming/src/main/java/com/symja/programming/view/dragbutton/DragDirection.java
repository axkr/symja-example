package com.symja.programming.view.dragbutton;

/**
 * See the trigonometric circle
 * https://developer.apple.com/documentation/uikit/uibezierpath/1624358-bezierpathwitharccenter
 */
public enum DragDirection {
    TOP_LEFT(Math.PI, 3.0 / 2.0 * Math.PI),
    TOP_RIGHT(3.0 / 2.0 * Math.PI, Math.PI * 2),
    RIGHT_BOTTOM(0, Math.PI / 2),
    LEFT_BOTTOM(Math.PI / 2, Math.PI),
    CENTER(0, Math.PI * 2);

    private final double fromRadians;
    private final double toRadians;

    DragDirection(double fromRadians, double toRadians) {
        this.fromRadians = fromRadians;
        this.toRadians = toRadians;
    }

    public static DragDirection getDirection(double angleInRadians) {
        angleInRadians = angleInRadians % (2 * Math.PI);
        for (DragDirection value : values()) {
            if (value.contains(angleInRadians)) {
                return value;
            }
        }
        return CENTER;
    }

    boolean contains(double angle) {
        return fromRadians <= angle && angle <= toRadians;
    }
}
