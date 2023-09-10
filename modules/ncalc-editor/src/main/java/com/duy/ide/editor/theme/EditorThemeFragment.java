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

package com.duy.ide.editor.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duy.editor.R;
import com.duy.ide.editor.theme.model.Constants;
import com.duy.ide.editor.theme.model.EditorTheme;
import com.duy.ide.editor.view.CodeEditor;
import com.jecelyin.editor.v2.Preferences;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class EditorThemeFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditorThemeAdapter editorThemeAdapter;
    private Preferences mPreferences;
    private ProgressBar progressBar;
    private LoadThemeTask mLoadThemeTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreferences = Preferences.getInstance(getContext());
        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        editorThemeAdapter = new EditorThemeAdapter();
        if (getActivity() instanceof EditorThemeAdapter.OnThemeSelectListener) {
            editorThemeAdapter.setOnThemeSelectListener((EditorThemeAdapter.OnThemeSelectListener) getActivity());
        }
        editorThemeAdapter.setHasStableIds(false);
        recyclerView.setAdapter(editorThemeAdapter);

        loadData();
    }

    @Override
    public void onDestroyView() {
        if (mLoadThemeTask != null) {
            mLoadThemeTask.cancel(true);
        }
        super.onDestroyView();
    }

    private void loadData() {
        mLoadThemeTask = new LoadThemeTask(getContext());
        mLoadThemeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private int findThemeIndex(EditorTheme editorTheme) {
        int position = editorThemeAdapter.getPosition(editorTheme);
        if (position < 0) {
            return 0;
        }
        return position;
    }

    public static class EditorThemeAdapter extends RecyclerView.Adapter<EditorThemeAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
        private final ArrayList<EditorTheme> editorThemes;
        private OnThemeSelectListener onThemeSelectListener;
        private String sampleCode;

        EditorThemeAdapter() {
            editorThemes = new ArrayList<>();
            resolveLanguage();
        }

        private void resolveLanguage() {
            sampleCode = "Package[ \n" +
                    "  \"Polynomials\", \n" +
                    "  (* define the public available symbols *)\n" +
                    "  {LaguerreP, LegendreP}, \n" +
                    "{ \n" +
                    "  (* Laguerre polynomials \n" +
                    "     http://en.wikipedia.org/wiki/Laguerre_polynomials *) \n" +
                    "  LaguerreP[0,x_]:=1, \n" +
                    "  LaguerreP[1,x_]:=1-x, \n" +
                    "  LaguerreP[n_?Integer,x_]:= \n" +
                    "      ExpandAll[(2*n-1-x)*LaguerreP[n-1,x] - (n-1)^2*LaguerreP[n-2,x]] /; NonNegative[n], \n" +
                    "  (* Legendre polynomials \n" +
                    "     http://en.wikipedia.org/wiki/Legendre_polynomials *)\n" +
                    "  LegendreP[n_?Integer,x_]:=\n" +
                    "      1/(2^n)*Sum[ExpandAll[Binomial[n,k]^2*(x-1)^(n-k)*(x+1)^k], {k,0,n}] /; NonNegative[n]\n" +
                    "    \n" +
                    "     HalfIntegerQ::usage = \"If m, n, ... are explicit half-integers, FractionQ[m,n,...] returns True; else it returns False.\";\n" +
                    "HalfIntegerQ[u__] := Scan[Function[If[Head[#]===Rational && Denominator[#]==2,Null,Return[False]]],{u}]===Null\n" +
                    "\n" +
                    "    \n" +
                    "} ]";
            sampleCode = sampleCode.replace("\r\n", "\n");
            sampleCode = sampleCode.replace("\r", "\n");
        }

        int getPosition(EditorTheme editorTheme) {
            return editorThemes.indexOf(editorTheme);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theme, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("UseSparseArrays")
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final EditorTheme editorTheme = editorThemes.get(position);

            final String title = makeTitle(position, editorTheme);
            holder.mTxtName.setText(title);

            CodeEditor editorView = holder.mEditorView;
            editorView.setTheme(editorTheme);
            editorView.getDocument().setMode("symja");

            String sampleData = getSampleData();
            editorView.setText(sampleData);
            holder.mBtnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onThemeSelectListener != null) {
                        onThemeSelectListener.onEditorThemeSelected(editorTheme);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return editorThemes.size();
        }

        private String makeTitle(int position, EditorTheme editorTheme) {
            return (position + 1) + ". " + editorTheme.getName();
        }

        private String getSampleData() {
            if (sampleCode == null) {
                sampleCode = Constants.C_PLUS_PLUS_SAMPLE;
            }
            return sampleCode;
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            return editorThemes.get(position).getName();
        }

        void setOnThemeSelectListener(OnThemeSelectListener onThemeSelectListener) {
            this.onThemeSelectListener = onThemeSelectListener;
        }

        void addTheme(EditorTheme theme) {
            editorThemes.add(theme);
            notifyItemInserted(editorThemes.size() - 1);
        }

        public interface OnThemeSelectListener {
            void onEditorThemeSelected(EditorTheme theme);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View mBtnSelect;
            CodeEditor mEditorView;
            TextView mTxtName;

            ViewHolder(View itemView) {
                super(itemView);
                setIsRecyclable(false);
                mEditorView = itemView.findViewById(R.id.editor_view);
                mTxtName = itemView.findViewById(R.id.txt_name);
                mBtnSelect = itemView.findViewById(R.id.btn_select);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadThemeTask extends AsyncTask<Void, EditorTheme, Void> {
        private Context context;
        private AssetManager mAssetManager;

        LoadThemeTask(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String[] list = mAssetManager.list(ThemeLoader.ASSET_PATH);
                if (list != null) {
                    Arrays.sort(list);
                    for (String name : list) {
                        if (isCancelled()) {
                            return null;
                        }
                        Thread.sleep(1);
                        EditorTheme theme = ThemeLoader.getTheme(context, name);
                        publishProgress(theme);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAssetManager = getContext().getAssets();

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled()) {
                return;
            }
            recyclerView.scrollToPosition(findThemeIndex(mPreferences.getEditorTheme()));
            progressBar.setVisibility(View.GONE);

        }

        @Override
        protected void onProgressUpdate(EditorTheme... themes) {
            super.onProgressUpdate(themes);
            try {
                editorThemeAdapter.addTheme(themes[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
