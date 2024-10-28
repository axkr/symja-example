package com.symja.programming.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.symja.programming.R;

public class ErrorHandler {
    public static String getErrorMessage(Context context, @NonNull Throwable e) {
        String localizedMessage = e.getLocalizedMessage();
        if (localizedMessage != null) {
            return localizedMessage;
        }

        String message = e.getMessage();
        if (message != null) {
            return message;
        }
        return context.getString(R.string.symja_prgm_error_unknown);
    }
}
