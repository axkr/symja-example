/*
 * Copyright (c) 2017 by Tran Le Duy
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

package com.symja.common.logging;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.common.BuildConfig;

/**
 * Android logcat
 * Setup Crashlytics https://firebase.google.com/docs/crashlytics/get-started?authuser=0
 */
public class DLog {
    /**
     * Show log
     */
    public static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "DLog";
    public static boolean TEST_MODE = false;
    public static int UITestmode = 0;
    /**
     * Android environment
     */
    public static boolean ANDROID = true;

    /**
     * Debug log
     * <p>
     * -
     */
    public static void d(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * debug log
     */
    public static void d(String TAG, Object message) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(TAG, message.toString());
            } else {
                System.out.println(TAG + ": " + message.toString());
            }
        }
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(tag, message, throwable);
            } else {
                System.out.println(TAG + ": " + message);
            }
        }
    }

    /**
     * warning log
     */
    public static void w(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.w(TAG, msg.toString());
            } else {
                System.err.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * warning log
     */
    public static void w(String tag, Object obj) {
        if (DEBUG) {
           if (obj instanceof Throwable) {
               w(tag, ((Throwable) obj).getMessage(), (Throwable) obj);
           }
        }
    }

    /**
     * warning log
     */
    public static void w(String TAG, Object msg, Throwable e) {
        if (DEBUG) {
            if (ANDROID) {
                Log.w(TAG, msg.toString(), e);
            } else {
                System.err.println(TAG + ": " + msg.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Error log
     */
    public static void e(Throwable exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, "Error ", exception);
            } else {
                System.err.println(TAG + ": " + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }

    /**
     * Error log
     */
    public static void e(String exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, exception);
            } else {
                System.err.println(TAG + ": " + exception);
            }
        }
    }

    /**
     * Error log
     */
    public static void e(String TAG, Throwable e) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, "Error ", e);
            } else {
                System.err.println(TAG + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * error log
     */
    public static void e(@NonNull String TAG, @NonNull String exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, exception);
            } else {
                System.err.println(TAG + ": " + exception);
            }
        }
    }

    /**
     * error log
     */
    public static void e(String message, Object... args) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, String.format(message, args));
            } else {
                System.err.println(TAG + ": " + String.format(message, args));
            }
        }
    }

    /**
     * Error log
     */
    public static void e(@NonNull String TAG, @NonNull String msg, @NonNull Throwable e) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, msg, e);
            } else {
                System.err.println(TAG + ": " + msg);
                e.printStackTrace();
            }
        }
    }

    /**
     * info log
     */
    public static void i(@NonNull Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.i(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * info log
     */
    public static void i(String tag, @Nullable Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.i(tag, String.valueOf(msg));
            } else {
                System.err.println(tag + ": " + msg);
            }
        }
    }

    public static boolean isUITestingMode() {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            UITestmode = 1;
            return true;
        } catch (ClassNotFoundException e) {
            UITestmode = 2;
            return false;
        }
    }
}
