/*
 * Copyright (c) 2018 by Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.symja.programming.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

/**
 * Created by Duy on 2/5/2018.
 */

public class ViewUtils {

    /**
     * Show the view or text notification for a short period of time.  This time
     * could be user-definable.  This is the default.
     *
     * @see Toast#setDuration
     */
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    /**
     * Show the view or text notification for a long period of time.  This time
     * could be user-definable.
     *
     * @see Toast#setDuration
     */
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    public static float oneDpInPx = 1;

    public static void init(Context context) {
        oneDpInPx = ViewUtils.dpToPx(context, 1);
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int spToPx(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static float pxToDp(Context context, int px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int getColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static boolean getBoolean(Context context, int attr, boolean def) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        boolean value = a.getBoolean(0, def);
        a.recycle();
        return value;
    }

    @Nullable
    public static ColorStateList getColorStateList(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        ColorStateList color = a.getColorStateList(0);
        a.recycle();
        return color;
    }

    public static boolean showDialog(Activity activity, Dialog dialog) {
        if (activity == null) {
            return false;
        }
        if (!activity.isFinishing()) {
            dialog.show();
            return true;
        }
        return false;
    }


    @SuppressWarnings("UnusedReturnValue")
    public static boolean showToast(@Nullable Context activity, @StringRes int idRes, int time) {
        return showToast(activity, activity != null ? activity.getString(idRes) : null, time);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean showToast(@Nullable Context activity, String idRes, int time) {
        if (activity == null) {
            return false;
        }
        if (activity instanceof Activity && ((Activity) activity).isFinishing()) {
            return false;
        }
        Toast.makeText(activity, idRes, time).show();
        return true;
    }

    public static void hideKeyboard(@NonNull Context context, @NonNull View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * https://stackoverflow.com/questions/2403632/android-show-soft-keyboard-automatically-when-focus-is-on-an-edittext
     */
    public static void showKeyboard(@NonNull Context context, @NonNull View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    public static void hideView(@Nullable final View view) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        view.animate().alpha(0).setDuration(300).setListener(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                view.setVisibility(View.GONE);
            }
        }).start();
    }

    public static void showView(@Nullable final View view) {
        if (view == null) {
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.animate().cancel();
        view.setAlpha(0);
        view.animate().alpha(1).setDuration(300).setListener(new AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                view.setAlpha(1);
            }
        }).start();
    }

    public static void showMessageDialog(@NonNull Context context, @NonNull String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    public static void showConfirmationDialog(@NonNull Context context, @NonNull String message,
                                              Consumer<Boolean> callback) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    callback.accept(true);
                    dialog.cancel();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    callback.accept(false);
                    dialog.cancel();
                })
                .create()
                .show();
    }

    /**
     * <a href="https://stackoverflow.com/a/48452309">...</a>
     * <p>
     * Returns array of views [x, y] position relative to parent
     */
    public static int[] getPositionInParent(ViewGroup parent, View view) {
        int relativePosition[] = {view.getLeft(), view.getTop()};
        ViewGroup currentParent = (ViewGroup) view.getParent();
        while (currentParent != parent) {
            relativePosition[0] += currentParent.getLeft();
            relativePosition[1] += currentParent.getTop();
            currentParent = (ViewGroup) currentParent.getParent();
        }
        return relativePosition;
    }
}
