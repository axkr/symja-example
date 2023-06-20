package com.symja.app;

import android.app.Application;

import org.matheclipse.core.basic.AndroidLoggerFix;

public class SymjaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidLoggerFix.fix();
    }
}
