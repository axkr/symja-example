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

package com.symja.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.appcompat.app.AppCompatActivity;

import com.symja.common.logging.DLog;

/**
 * Created by Duy on 06-May-18.
 */

public class SystemUiUtils {

    private static final String TAG = "SystemUiUtils";

    public static boolean setNavigationBarColor(AppCompatActivity context, Drawable drawable) {
        if (drawable == null) {
            return false;
        }
        if (drawable instanceof ColorDrawable) {
            int color = ((ColorDrawable) drawable).getColor();
            if (color != Color.TRANSPARENT) {
                context.getWindow().setNavigationBarColor(color);
                return true;
            }
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            int size = layerDrawable.getNumberOfLayers();
            Drawable foreground = layerDrawable.getDrawable(size - 1);
            if (foreground instanceof BitmapDrawable) {
                if (size >= 2) {
                    return setNavigationBarColor(context, layerDrawable.getDrawable(size - 2));
                }
            }
            return setNavigationBarColor(context, foreground);
        } else {
            Rect padding = new Rect();
            drawable.getPadding(padding);
            int width = padding.left + 128 + padding.right;
            int height = padding.top + 128 + padding.bottom;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);

            int leftBottomColor = bitmap.getPixel(width - padding.right - 1, height - padding.bottom - 1);
            int rightBottomColor = bitmap.getPixel(padding.left + 1, height - padding.bottom - 1);
            bitmap.recycle();
            if (leftBottomColor != rightBottomColor) {
                if (DLog.DEBUG)
                    DLog.w(TAG, "setNavigationBarColor: can not resolve bottom color of " + drawable);
                return false;
            }

            context.getWindow().setNavigationBarColor(leftBottomColor);
            return true;
        }
        return false;
    }
}
