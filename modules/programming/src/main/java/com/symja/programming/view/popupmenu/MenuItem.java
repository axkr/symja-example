package com.symja.programming.view.popupmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

public class MenuItem {
    public static final Integer NO_ID = -1;

    @IdRes
    private Integer id = NO_ID;

    @Nullable
    private CharSequence title;
    @Nullable
    private CharSequence subtitle;
    @Nullable
    @StringRes
    private Integer titleId;
    @Nullable
    @StringRes
    private Integer subtitleId;

    @Nullable
    private Drawable icon;
    @Nullable
    private Integer iconId;

    private boolean enable = true;

    @Nullable
    private Object tag;

    public MenuItem(@Nullable @IdRes Integer id, @Nullable CharSequence title, @Nullable CharSequence subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        this.id = id;
    }

    public MenuItem(@IdRes @Nullable Integer id, @Nullable @StringRes Integer titleId, @StringRes @Nullable Integer subtitleId) {
        this.titleId = titleId;
        this.subtitleId = subtitleId;
        this.id = id;
    }

    public MenuItem(@IdRes @Nullable Integer id, @Nullable CharSequence title) {
        this.title = title;
        this.id = id;
    }

    @Nullable
    public CharSequence getTitle() {
        return title;
    }

    @Nullable
    public CharSequence getSubtitle() {
        return subtitle;
    }

    @Nullable
    public Integer getId() {
        return id;
    }

    @Nullable
    public Integer getTitleId() {
        return titleId;
    }

    @Nullable
    public Integer getSubtitleId() {
        return subtitleId;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isEnabled() {
        return enable;
    }

    @Nullable
    public CharSequence getTitle(@NonNull Context context) {
        if (title != null) {
            return title;
        }
        if (titleId != null) {
            return context.getString(titleId);
        }
        return null;
    }

    @Nullable
    public CharSequence getSubtitle(@NonNull Context context) {
        if (subtitle != null) {
            return subtitle;
        }
        if (subtitleId != null) {
            return context.getString(subtitleId);
        }
        return null;
    }

    public Integer getItemId() {
        return id;
    }

    public void setIcon(@Nullable Drawable icon) {
        this.icon = icon;
    }

    public void setIcon(@Nullable Integer iconId) {
        this.iconId = iconId;
    }

    public void setIconId(@Nullable Integer iconId) {
        this.iconId = iconId;
    }

    @Nullable
    public Drawable getIcon(Context context) {
        if (icon != null) {
            return icon;
        }
        if (iconId != null) {
            return ContextCompat.getDrawable(context, iconId);
        }
        return null;
    }

    public void setTag(@Nullable Object tag) {
        this.tag = tag;
    }

    @Nullable
    public Object getTag() {
        return tag;
    }
}
