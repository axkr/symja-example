package com.symja.common.analyst;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppAnalytics {

    @Nullable
    public static OnLogEvent action;

    private final Context context;

    public AppAnalytics(Context context) {
        this.context = context;
    }

    @NonNull
    public static AppAnalytics getInstance(@NonNull Context context) {
        return new AppAnalytics(context);
    }

    public void logEvent(@NonNull String event, @Nullable Bundle bundle) {
        if (action != null) {
            action.logEvent(context, event, bundle);
        }
    }

    public void logEvent(@NonNull String event) {
        if (action != null) {
            action.logEvent(context, event, null);
        }
    }

    public interface OnLogEvent {
        void logEvent(Context context, String event, @Nullable Bundle bundle);
    }
}
