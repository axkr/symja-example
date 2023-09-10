package com.symja.programming.view.dragbutton;

import androidx.annotation.Nullable;

import java.util.HashMap;

public interface DragView {
    void setOnDragListener(@Nullable DragListener listener);

    void setVibrateOnDrag(boolean vibrateOnDrag);

    HashMap<DragDirection, String> getDirectionTextMap();

    void setDirectionTextMap(HashMap<DragDirection, String> directionTextMap);
}