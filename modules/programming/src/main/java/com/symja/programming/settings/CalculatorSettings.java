/*
 * Copyright (C) 2018 Duy Tran Le
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.symja.programming.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.symja.common.logging.DLog;
import com.symja.programming.R;

/**
 * Setting for application
 * <p>
 * Created by Duy on 3/7/2016
 */
public class CalculatorSettings implements IProgrammingSettings{
    private static final String TAG = "CalculatorSettings";

    private final SharedPreferences preferences;
    private Context context;

    public CalculatorSettings(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public CalculatorSettings(SharedPreferences preferences, Context context) {
        this.context = context;
        this.preferences = preferences;
    }

    public static CalculatorSettings newInstance(Context c) {
        return new CalculatorSettings(c);
    }

    public boolean useLightTheme() {
        return preferences.getBoolean("THEME_STYLE", false);
    }

    @Override
    public boolean isUseRelaxedSyntax() {
        return getBoolean(getStringRes(R.string.pref_key_programming_relaxed_syntax), true);
    }

    @Override
    public boolean isDominantImplicitTimes() {
//        Config.DOMINANT_IMPLICIT_TIMES = false
        return getBoolean(context.getString(R.string.pref_key_dominant_implicit_times), false);
    }

    @Override
    public boolean isExplicitTimesOperator() {
//        Config.EXPLICIT_TIMES_OPERATOR = false
        return getBoolean(context.getString(R.string.pref_key_explicit_times_operator), false);
    }

    @Override
    public int getRecursionLimit() {
        return getInt(getStringRes(R.string.pref_key_recursion_limit), 256);
    }

    @Override
    public int getIterationLimit() {
        return getInt(getStringRes(R.string.pref_key_iteration_limit), 100000);
    }

    @Override
    public int getIntegrateRecursionLimit() {
        return getInt(getStringRes(R.string.pref_key_integrate_recursion_limit), 10);
    }

    @Override
    public int getLHospitalLimit() {
//        Config.LIMIT_LHOSPITAL_RECURSION_LIMIT = 128
        return getInt(getStringRes(R.string.pref_key_lhospital_limit), 128);
    }

    @Override
    public int getMaxAstSize() {
        //  Config.MAX_AST_SIZE = ((int) Short.MAX_VALUE) * 8
        return getInt(getStringRes(R.string.pref_key_max_ast_size), Short.MAX_VALUE * 8);
    }

    @Override
    public int getMaxOutputSize() {
        //  Config.MAX_OUTPUT_SIZE = Short.MAX_VALUE;
        return getInt(getStringRes(R.string.pref_key_max_output_size), Short.MAX_VALUE);
    }

    @Override
    public String getLastEditedDocumentName() {
        return getString(getStringRes(R.string.pref_key_last_edited_document), "");
    }

    @Override
    public void setLastEditedDocument(String lastEditedDocument) {
        putString(getStringRes(R.string.pref_key_last_edited_document), lastEditedDocument);
    }

    @Override
    public boolean isShowSymbolBar() {
        return getBoolean(getStringRes(R.string.pref_key_show_symbol_bar), true);
    }

    @Nullable
    @Override
    public String getSymjaServer() {
        return getString(getStringRes(R.string.pref_key_symjatalk_server), null);
    }

    @Override
    public boolean isUseSymjaTalkOffline() {
        return getBoolean(getStringRes(R.string.pref_key_symjatalk_offline_mode), false);
    }

    @Override
    public void put(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public void putString(String key, String value) {
        put(key, value);
    }

    public void put(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void put(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void put(String key, float value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int def) {
        try {
            return preferences.getInt(key, def);
        } catch (Exception e) {
            if (DLog.DEBUG) {
                Log.w(TAG, key + ": " +  e.getMessage());
            }
            try {
                String value = getString(key);
                return Integer.parseInt(value);
            } catch (Exception e1) {
                if (DLog.DEBUG) {
                    Log.w(TAG, key + ": " +e1.getMessage());
                }
            }
        }
        return def;
    }

    /**
     * get long value from key,
     *
     * @param key - key
     * @return -1 if not found
     */
    public long getLong(String key) {
        try {
            return preferences.getLong(key, -1);
        } catch (Exception e) {
            return -1;
        }
    }

    public String getString(String key) {
        try {
            return preferences.getString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getString(String key, String def) {
        try {
            return preferences.getString(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener
                                                                 onSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener
                                                                   onSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);

    }

    public void putString(String key, @NonNull Object value) {
        preferences.edit().putString(key, value.toString()).apply();
    }

    private String getStringRes(@StringRes int id) {
        return context.getString(id);
    }


}