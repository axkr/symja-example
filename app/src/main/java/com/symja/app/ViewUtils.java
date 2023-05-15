package com.symja.app;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class ViewUtils {

    public static void hideKeyboard(@NonNull View view) {
        hideKeyboard(view, /* useWindowInsetsController= */ true);
    }

    public static void hideKeyboard(@NonNull View view, boolean useWindowInsetsController) {
        if (useWindowInsetsController) {
            WindowInsetsControllerCompat windowController = ViewCompat.getWindowInsetsController(view);
            if (windowController != null) {
                windowController.hide(WindowInsetsCompat.Type.ime());
                return;
            }
        }
        InputMethodManager imm = getInputMethodManager(view);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Nullable
    private static InputMethodManager getInputMethodManager(@NonNull View view) {
        return view.getContext().getSystemService(InputMethodManager.class);
    }

}
