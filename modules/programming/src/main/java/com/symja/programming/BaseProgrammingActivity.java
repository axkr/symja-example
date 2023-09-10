package com.symja.programming;


import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.symja.common.utils.SystemUiUtils;


public abstract class BaseProgrammingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void restart() {
        Intent intent = new Intent(this, this.getClass());
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    protected void changeSystemBarColor() {
        int[] attrs = new int[]{android.R.attr.windowBackground};
        TypedArray arr = obtainStyledAttributes(attrs);
        for (int i = 0; i < attrs.length; i++) {
            try {
                Drawable drawable = null;
                try {
                    drawable = arr.getDrawable(i);
                } catch (Exception e) {
                    int resourceId = arr.getResourceId(i, -1);
                    if (resourceId != -1) {
                        drawable = ContextCompat.getDrawable(this, resourceId);
                    }
                }
                if (SystemUiUtils.setNavigationBarColor(this, drawable)) {
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        arr.recycle();
    }

}
