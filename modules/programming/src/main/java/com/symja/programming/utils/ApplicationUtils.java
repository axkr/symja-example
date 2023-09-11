package com.symja.programming.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import com.symja.common.utils.StoreUtil;


public class ApplicationUtils {

    public static void openUrl(Context context, String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            } catch (Exception e2) {
                ViewUtils.showToast(context, e2.getMessage(), ViewUtils.LENGTH_SHORT);
            }
        }

    }

    public static void feedback(Activity context) {
        StoreUtil.emailToDevelopers(context);
    }
}
