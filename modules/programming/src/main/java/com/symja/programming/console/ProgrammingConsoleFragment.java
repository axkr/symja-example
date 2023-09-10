package com.symja.programming.console;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import com.symja.common.analyst.AppAnalytics;
import com.symja.common.analyst.AppAnalyticsEvents;
import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;
import com.symja.common.logging.DLog;
import com.symja.evaluator.SymjaResult;
import com.symja.evaluator.config.EvaluationConfig;
import com.symja.programming.BaseProgrammingFragment;
import com.symja.programming.BuildConfig;
import com.symja.programming.ProgrammingContract;
import com.symja.programming.ProgrammingPresenter;
import com.symja.programming.R;
import com.symja.programming.autocomplete.FunctionSuggestionAdapter;
import com.symja.programming.autocomplete.SuggestionItem;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.console.models.ResultDetector;
import com.symja.programming.document.MarkdownDocumentActivity;
import com.symja.programming.document.model.DocumentItem;
import com.symja.programming.utils.ViewUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;


public class ProgrammingConsoleFragment extends BaseProgrammingFragment implements FunctionSuggestionAdapter.OnSuggestionClickListener,
        ProgrammingPresenter.ResultCallback,
        ProgrammingContract.IConsoleView {

    public static final String TAG = "ProgrammingConsoleFragment";
    private static final String EXTRA_INPUT = "ProgrammingConsoleDocument.EXTRA_INPUT";


    @Nullable
    private ProgrammingContract.IPresenter presenter;
    private ProgrammingDocumentManager manager;
    private ProgrammingConsoleDocument editingDocument;

    private ProgrammingResultAdapter programmingResultAdapter;

    @NonNull
    public static ProgrammingConsoleFragment newInstance(@Nullable String input) {

        Bundle args = new Bundle();
        args.putString(ProgrammingConsoleFragment.EXTRA_INPUT, input);
        ProgrammingConsoleFragment fragment = new ProgrammingConsoleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_programming_console, container, false);
    }

    @Override
    public void onDestroyView() {
        saveCurrentDocument();
        saveCurrentInput();
        super.onDestroyView();
    }

    @Override
    protected ProgrammingResultAdapter getResultAdapter() {
        if (programmingResultAdapter == null) {
            //noinspection ConstantConditions
            @NonNull Context context = getActivity() != null ? getActivity() : getContext();
            //noinspection ConstantConditions
            programmingResultAdapter = new ProgrammingResultAdapter(context, editingDocument);
            programmingResultAdapter.setProgrammingItemClickListener(this);
        }
        return programmingResultAdapter;
    }

    @Override
    protected void clickRun() {
        final String input = inputView.getText().toString();
        if (input.isEmpty()) {
            return;
        }
        AppAnalytics.getInstance(getContext()).logEvent(AppAnalyticsEvents.consoleClickRun);

        saveCurrentDocument();
        saveCurrentInput();
        prepareUIForCalculating();
        performCalculate(input);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void clickClearAll() {
        super.clickClearAll();
        if (getActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.programming_clear_all_items)
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    editingDocument.clear();
                    programmingResultAdapter.notifyDataSetChanged();
                    dialog.cancel();
                })
                .create()
                .show();

    }

    @Override
    public void clickOpenDocument(SuggestionItem item) {
        if (item.getAssetPath() == null) {
            return;
        }
        DocumentItem documentItem = new DocumentItem(item.getAssetPath(), item.getName(), item.getDescription());
        boolean displayed = false;
        if (presenter != null) {
            if (presenter.openDocument(documentItem)) {
                displayed = true;
            }
        }
        if (!displayed) {
            MarkdownDocumentActivity.open(this, documentItem);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        loadEditingDocument();

        super.onViewCreated(view, savedInstanceState);

        loadSavedInput();
        if (presenter == null) {
            setPresenter(new ProgrammingPresenter());
        }
    }

    private void loadSavedInput() {
        inputView.setText(settings.getString(EXTRA_INPUT, ""));
    }


    private void updateUICalculateComplete(@Nullable CalculationItem entry) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "updateUICalculateComplete() called with: entry = [" + entry + "]");
        }
        finishCalculating();
        if (entry != null) {

            editingDocument.add(0, entry);
            programmingResultAdapter.notifyItemInserted(0);

            while (editingDocument.size() > 100) {
                editingDocument.remove(editingDocument.size() - 1);
                programmingResultAdapter.notifyItemRemoved(editingDocument.size() - 1);
            }

            listResultView.scrollToPosition(0);
            ViewUtils.hideKeyboard(inputView.getContext(), inputView);
        }
    }

    private void finishCalculating() {
        if (DLog.DEBUG) {
            DLog.d(TAG, "finishCalculating() called");
        }
        progressBar.setVisibility(View.GONE);
        btnRun.setVisibility(View.VISIBLE);
    }

    private void prepareUIForCalculating() {
        btnRun.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void saveCurrentDocument() {
        try {
            settings.setLastEditedDocument(editingDocument.getName());
            manager.saveDocument(editingDocument);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentInput() {
        settings.putString(EXTRA_INPUT, inputView.getText().toString());
    }

    private void loadEditingDocument() {
        //noinspection ConstantConditions
        manager = new ProgrammingDocumentManager(getContext());
        String lastEditedDocumentName = settings.getLastEditedDocumentName();
        if (lastEditedDocumentName != null && !lastEditedDocumentName.isEmpty()) {
            try {
                this.editingDocument = manager.readDocument(lastEditedDocumentName);
            } catch (Exception e) {
                this.editingDocument = manager.createNewDocument();
            }
        } else {
            this.editingDocument = manager.createNewDocument();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean firstLaunch = preferences.getBoolean("console_first_launch", true);
        if (firstLaunch && (BuildConfig.DEBUG && editingDocument.isEmpty())) {
            try {
                String content = IOUtils.toString(getContext().getAssets().open("console/Document0.json"));
                JSONObject jsonObject = new JSONObject(content);
                ProgrammingConsoleDocument examples = new ProgrammingConsoleDocument(jsonObject.toMap());
                this.editingDocument.addAll(examples);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void performCalculate(@NonNull final String input) {
        if (presenter != null) {
            EvaluationConfig config=  EvaluationConfig.newInstance();
            // TODO: load config form settings config = EvaluationConfig.loadFromSetting(getContext());
            config.setEvalMode(EvaluationConfig.CalculationMode.SYMBOLIC);
            AsyncTask<Void, Void, SymjaResult> task = presenter.createCalculateTask(
                    this, input, config);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onSuccess(@NonNull SymjaResult result, String expression) {
        if (getContext() == null) {
            return;
        }
        CalculationItem item = ResultDetector.resolve(result, expression);
        updateUICalculateComplete(item);
    }

    @Override
    public void onError(@Nullable Throwable error, String expression) {
        if (getContext() == null) {
            return;
        }
        if (error != null && error.getMessage() != null) {
            String message = error.getMessage();
            SpannableStringBuilder textContent = new SpannableStringBuilder(message);
            textContent.setSpan(new TypefaceSpan("monospace"),
                    0, textContent.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            inputView.requestFocus();
            // TODO: handle error inputView.setError(textContent);

            if (BuildConfig.DEBUG) {
                error.printStackTrace();
            }
        }
        finishCalculating();
    }

    @Override
    public void setPresenter(@Nullable ProgrammingContract.IPresenter presenter) {
        this.presenter = presenter;
    }

}
