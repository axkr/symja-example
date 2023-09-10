package com.symja.programming.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.symja.common.utils.StoreUtil;


public class ApplicationUtils {

    public static void openUrl(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            ViewUtils.showToast(context, e.getMessage(), ViewUtils.LENGTH_SHORT);
        }
    }

    public static void feedback(Activity context) {
        StoreUtil.emailToDevelopers(context);
    }
}
