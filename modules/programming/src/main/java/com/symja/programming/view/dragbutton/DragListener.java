package com.symja.programming.view.dragbutton;

import androidx.annotation.NonNull;

public interface DragListener {

    @SuppressWarnings("UnusedReturnValue")
    boolean onDrag(@NonNull DragButton view, @NonNull DragDirection direction, String text, DragButton.State state);
}