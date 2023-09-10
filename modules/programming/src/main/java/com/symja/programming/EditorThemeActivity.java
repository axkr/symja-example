/*
 * Copyright (C) 2018 Tran Le Duy
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

//package com.symja.programming;
//
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//
//import com.symja.programming.R;
//import com.duy.ide.editor.theme.EditorThemeFragment;
//import com.duy.ide.editor.theme.model.EditorTheme;
//import com.jecelyin.editor.v2.Preferences;
//
//public class EditorThemeActivity extends BaseProgrammingActivity
//        implements EditorThemeFragment.EditorThemeAdapter.OnThemeSelectListener {
//    private Preferences preferences;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_editor_theme);
//        setupToolbar();
//        setTitle(R.string.programming_editor_theme);
//
//        preferences = Preferences.getInstance(this);
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.content, new EditorThemeFragment())
//                .commit();
//    }
//
//    @Override
//    public void onEditorThemeSelected(EditorTheme theme) {
//        preferences.setEditorTheme(theme.getFileName());
//        Toast.makeText(this, getString(R.string.selected), Toast.LENGTH_SHORT).show();
//        setResult(RESULT_OK);
//    }
//
//}
