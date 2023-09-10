package com.symja.programming.view.popupmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.symja.common.logging.DLog;

import java.lang.reflect.Field;

public class AutoCloseablePopupWindow extends PopupWindow implements DefaultLifecycleObserver {

    private static final String TAG = "AutoCloseablePopupWindow";
    protected FragmentActivity context;

    public AutoCloseablePopupWindow(FragmentActivity context) {
        super(context);
        this.context = context;
    }

    public static void hideSystemUiIfNeeded(@NonNull Activity activity, @NonNull PopupWindow popupWindow) {
        try {
            //noinspection JavaReflectionMemberAccess
            @SuppressLint("DiscouragedPrivateApi") Field field = PopupWindow.class.getDeclaredField("mDecorView");
            field.setAccessible(true);
            Object object = field.get(popupWindow);
            if (object instanceof View) {
                View decorView = (View) object;
                decorView.setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FragmentActivity getContext() {
        return context;
    }

    @CallSuper
    @Override
    public void showAsDropDown(View anchor) {
        Lifecycle lifecycle = context.getLifecycle();
        if (!lifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return;
        }

        if (context != null) {
            context.getLifecycle().removeObserver(this);
            context.getLifecycle().addObserver(this);
        }
        boolean focusable = isFocusable();
        boolean touchable = isTouchable();
        boolean outsideTouchable = isOutsideTouchable();
        setFocusable(false);
        setTouchable(false);
        setOutsideTouchable(false);

        hideSystemUiIfNeeded(context, this);

        try {
            super.showAsDropDown(anchor);
        } catch (Exception e) {
            e.printStackTrace();
            if (DLog.DEBUG) {
                throw e;
            }
        }
        hideSystemUiIfNeeded(context, this);

        setFocusable(focusable);
        setTouchable(touchable);
        setOutsideTouchable(outsideTouchable);
        update();
    }

    @CallSuper
    @Override
    public void dismiss() {
        if (context != null) {
            context.getLifecycle().removeObserver(this);
        }

        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        if (DLog.DEBUG) DLog.d(TAG, "onStop() called");
        this.dismiss();
    }

}
