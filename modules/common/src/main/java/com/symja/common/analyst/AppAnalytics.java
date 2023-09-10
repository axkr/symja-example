package com.symja.common.analyst;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppAnalytics {
    public static AppAnalytics getInstance(Context context) {
        return new AppAnalytics();
    }

    public void logEvent(@NonNull String event, @Nullable Bundle bundle) {
    }
    public void logEvent(@NonNull String event) {
    }
}
