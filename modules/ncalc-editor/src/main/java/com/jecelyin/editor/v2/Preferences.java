/*
 * Copyright 2018 Mr Duy
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

package com.jecelyin.editor.v2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.duy.common.utils.DLog;
import com.duy.editor.R;
import com.duy.ide.editor.theme.ThemeLoader;
import com.duy.ide.editor.theme.model.EditorTheme;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_FONT_SIZE = "pref_font_size";
    public static final String KEY_WORD_WRAP = "pref_word_wrap";
    public static final String KEY_SHOW_LINE_NUMBER = "pref_show_linenumber";
    public static final String KEY_SHOW_WHITESPACE = "pref_show_whitespace";
    public static final String KEY_AUTO_INDENT = "pref_auto_indent";
    public static final String KEY_TAB_SIZE = "pref_tab_size";
    public static final String KEY_SYMBOL = "pref_symbol";
    public static final String KEY_AUTO_CAPITALIZE = "pref_auto_capitalize";
    public static final String KEY_SCREEN_ORIENTATION = "pref_screen_orientation";
    public static final String KEY_KEEP_SCREEN_ON = "pref_keep_screen_on";
    public static final String KEY_READ_ONLY = "readonly_mode";

    public static final int DEF_MIN_FONT_SIZE = 9;
    public static final int DEF_MAX_FONT_SIZE = 32;

    public static final int SCREEN_ORIENTATION_AUTO = 0;
    public static final int SCREEN_ORIENTATION_LANDSCAPE = 1;
    public static final int SCREEN_ORIENTATION_PORTRAIT = 2;
    public static final String VALUE_SYMBOL = TextUtils.join("\n", new String[]{"{", "}", "<", ">"
            , ",", ";", "'", "\"", "(", ")", "/", "\\", "%", "[", "]", "|", "#", "=", "$", ":"
            , "&", "?", "!", "@", "^", "+", "*", "-", "_", "`", "\\t", "\\n"});
    public static final String KEY_AUTO_PAIR = "pref_auto_pair";
    private static final String KEY_TOUCH_TO_ADJUST_TEXT_SIZE = "pref_touch_to_adjust_text_size";
    private static final String KEY_HIGHLIGHT_FILE_SIZE_LIMIT = "pref_highlight_file_size_limit";
    private static final String KEY_REMEMBER_LAST_OPENED_FILES = "pref_remember_last_opened_files";
    private static final String KEY_TOOLBAR_ICONS = "pref_toolbar_icons";
    private static final String KEY_LAST_OPEN_PATH = "last_open_path";
    private static final String KEY_SHOW_HIDDEN_FILES = "show_hidden_files";
    private static final String KEY_FILE_SORT_TYPE = "show_file_sort";
    private static final String KEY_FULL_SCREEN = "fullscreen_mode";
    private static final String KEY_LAST_TAB = "last_tab";
    private static final Object mContent = new Object();
    private final SharedPreferences preferences;

    private final Map<String, Object> map;
    private final Context context;
    private final WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    private Set<String> toolbarIcons;

    public Preferences(Context context) {
        this.context = context;
        PreferenceManager.setDefaultValues(context, R.xml.preference_editor, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        map = new HashMap<>();
        map.put(KEY_FONT_SIZE, 13);
        map.put(KEY_TOUCH_TO_ADJUST_TEXT_SIZE, true);
        map.put(KEY_WORD_WRAP, true);
        map.put(KEY_SHOW_LINE_NUMBER, true);
        map.put(KEY_SHOW_WHITESPACE, true);

        map.put(context.getString(R.string.pref_auto_complete), true);
        map.put(KEY_AUTO_INDENT, true);
        map.put(KEY_AUTO_PAIR, false);
        map.put(context.getString(R.string.pref_auto_save), true);

        map.put(context.getString(R.string.pref_insert_space_for_tab), true);
        map.put(KEY_TAB_SIZE, 4);
        map.put(KEY_SYMBOL, VALUE_SYMBOL);
        map.put(KEY_AUTO_CAPITALIZE, false);
        map.put(context.getString(R.string.pref_volume_move), true);

        map.put(KEY_HIGHLIGHT_FILE_SIZE_LIMIT, 500);
        map.put(KEY_REMEMBER_LAST_OPENED_FILES, true);

        map.put(KEY_SCREEN_ORIENTATION, "auto");
        map.put(KEY_KEEP_SCREEN_ON, false);

        toolbarIcons = preferences.getStringSet(KEY_TOOLBAR_ICONS, null);
        map.put(KEY_LAST_OPEN_PATH, Environment.getExternalStorageDirectory().getPath());
        map.put(KEY_READ_ONLY, false);
        map.put(KEY_SHOW_HIDDEN_FILES, false);
        map.put(KEY_FILE_SORT_TYPE, 0);
        map.put(KEY_FULL_SCREEN, false);
        map.put(KEY_LAST_TAB, 0);

        Map<String, ?> values = preferences.getAll();
        for (String key : map.keySet()) {
            updateValue(key, values);
        }
    }

    public static Preferences getInstance(Context context) {
        return new Preferences(context);
    }

    public int getMaxEditor() {
        return 3;
    }

    private void updateValue(String key, Map<String, ?> values) {
        Object value = map.get(key);
        if (value == null)
            return;
        Class cls = value.getClass();

        try {
            if (cls == int.class || cls == Integer.class) {
                Object in = values.get(key);
                if (in != null)
                    value = in instanceof Integer ? (int) in : Integer.parseInt(String.valueOf(in));
            } else if (cls == boolean.class || cls == Boolean.class) {
                Boolean b = (Boolean) values.get(key);
                value = b == null ? (boolean) value : b;
            } else {
                String str = (String) values.get(key);
                value = str == null ? (String) value : str;
            }
        } catch (Exception e) {
            DLog.e("key = " + key, e);
            return;
        }
        map.put(key, value);
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            mListeners.put(listener, mContent);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            mListeners.remove(listener);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateValue(key, sharedPreferences.getAll());
        Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners = mListeners.keySet();
        for (SharedPreferences.OnSharedPreferenceChangeListener listener : listeners) {
            if (listener != null) {
                listener.onSharedPreferenceChanged(sharedPreferences, key);
            }
        }
    }

    public boolean isShowLineNumber() {
        return (boolean) map.get(KEY_SHOW_LINE_NUMBER);
    }

    public boolean isShowWhiteSpace() {
        return (boolean) map.get(KEY_SHOW_WHITESPACE);
    }


    public int getHighlightSizeLimit() {
        return 1024 * (int) map.get(KEY_HIGHLIGHT_FILE_SIZE_LIMIT);
    }

    //auto save is default
    public boolean isAutoSave() {
        return (boolean) map.get(context.getString(R.string.pref_auto_save));
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            return preferences.getBoolean(key, def);
        } catch (ClassCastException e) {
            return Boolean.parseBoolean(preferences.getString(key, String.valueOf(def)));
        }
    }

    private String getString(String key, String def) {
        try {
            return preferences.getString(key, def);
        } catch (ClassCastException e) {
            return def;
        }
    }

    public boolean isKeepScreenOn() {
        return (boolean) map.get(KEY_KEEP_SCREEN_ON);
    }

    public Integer[] getToolbarIcons() {
        if (toolbarIcons == null)
            return null;
        Integer[] list = new Integer[toolbarIcons.size()];
        int i = 0;
        for (String id : toolbarIcons) {
            list[i++] = Integer.valueOf(id);
        }
        return list;
    }


    public Object getValue(String key) {
        return map.get(key);
    }

    public int getFontSize() {
        return (int) map.get(KEY_FONT_SIZE);
    }

    public boolean isReadOnly() {
        return false;
    }


    public boolean isAutoIndent() {
        return (boolean) map.get(KEY_AUTO_INDENT);
    }

    public boolean isAutoPair() {
        return (boolean) map.get(KEY_AUTO_PAIR);
    }

    public boolean isWordWrap() {
        return (boolean) map.get(KEY_WORD_WRAP);
    }

    public boolean isTouchScaleTextSize() {
        return (boolean) map.get(KEY_TOUCH_TO_ADJUST_TEXT_SIZE);
    }

    public boolean isAutoCapitalize() {
        return (boolean) map.get(KEY_AUTO_CAPITALIZE);
    }


    public int getTabSize() {
        return (int) map.get(KEY_TAB_SIZE);
    }

    @ScreenOrientation
    public int getScreenOrientation() {
        String ori = (String) map.get(KEY_SCREEN_ORIENTATION);
        if ("landscape".equals(ori)) {
            return SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equals(ori)) {
            return SCREEN_ORIENTATION_PORTRAIT;
        } else {
            return SCREEN_ORIENTATION_AUTO;
        }
    }

    public String getSymbol() {
        return (String) map.get(KEY_SYMBOL);
    }


    public boolean isFullScreenMode() {
        return (boolean) map.get(KEY_FULL_SCREEN);
    }


    /**
     * Theme and fonts
     */
    @NonNull
    public EditorTheme getEditorTheme() {
        String fileName = getString(context.getString(R.string.pref_theme_editor_theme), "");
        EditorTheme theme = ThemeLoader.getTheme(context, fileName);
        if (theme != null) {
            return theme;
        }
        return ThemeLoader.loadDefault(context);
    }

    public void setEditorTheme(String fileName) {
        this.preferences.edit().putString(this.context.getString(R.string.pref_theme_editor_theme), fileName).apply();
    }

    public boolean isUseVolumeToMove() {
        return preferences.getBoolean(context.getString(R.string.pref_volume_move), true);
    }

    public boolean isUseAutoComplete() {
        return getBoolean(context.getString(R.string.pref_auto_complete), true);
    }

    public boolean isInsertSpaceForTab() {
        return getBoolean(context.getString(R.string.pref_insert_space_for_tab), true);
    }


    @IntDef({SCREEN_ORIENTATION_AUTO, SCREEN_ORIENTATION_LANDSCAPE, SCREEN_ORIENTATION_PORTRAIT})
    public @interface ScreenOrientation {
    }
}
