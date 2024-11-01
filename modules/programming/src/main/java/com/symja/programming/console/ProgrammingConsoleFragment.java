package com.symja.programming.console;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.symja.common.analyst.AppAnalytics;
import com.symja.common.analyst.AppAnalyticsEvents;
import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;
import com.symja.common.logging.DLog;
import com.symja.evaluator.SymjaResult;
import com.symja.evaluator.config.SymjaEvaluationConfig;
import com.symja.programming.BaseProgrammingFragment;
import com.symja.programming.ProgrammingContract;
import com.symja.programming.ProgrammingPresenter;
import com.symja.programming.R;
import com.symja.programming.autocomplete.FunctionSuggestionAdapter;
import com.symja.programming.autocomplete.SuggestionItem;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.console.models.ResultDetector;
import com.symja.programming.document.MarkdownDocumentActivity;
import com.symja.programming.document.model.DocumentItem;
import com.symja.programming.utils.ErrorHandler;
import com.symja.programming.utils.ViewUtils;

import org.apache.commons.io.IOUtils;
import org.matheclipse.parser.client.SyntaxError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.rosemoe.sora.text.ContentLine;


public class ProgrammingConsoleFragment extends BaseProgrammingFragment implements FunctionSuggestionAdapter.OnSuggestionClickListener,
        ProgrammingPresenter.ResultCallback,
        ProgrammingContract.IConsoleView {

    public static final String TAG = "ProgrammingConsoleFragment";
    private static final String EXTRA_INPUT = "ProgrammingConsoleDocument.EXTRA_INPUT";


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
        return inflater.inflate(R.layout.symja_prgm_fragment_programming_console, container, false);
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
                .setTitle(R.string.symja_prgm_message_clear_all_items)
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
    public void clickOpenDocument(@NonNull SuggestionItem item) {
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
        if (firstLaunch && (editingDocument.isEmpty())) {
            try {
                String content = IOUtils.toString(getContext().getAssets().open("console/Document0.json"), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(content);
                ProgrammingConsoleDocument examples = new ProgrammingConsoleDocument(jsonObject.toMap());
                this.editingDocument.addAll(examples);
                preferences.edit().putBoolean("console_first_launch", false).apply();
            } catch (IOException e) {
                DLog.i(TAG, e.getMessage());
            } catch (Exception e) {
                DLog.w(TAG, e.getMessage());
            }
        }
    }

    private void performCalculate(@NonNull final String input) {
        if (presenter != null) {
            SymjaEvaluationConfig config = SymjaEvaluationConfig.newInstance();
            // TODO: load config form settings config = SymjaEvaluationConfig.loadFromSetting(getContext());
            config.setEvalMode(SymjaEvaluationConfig.CalculationMode.SYMBOLIC);
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
        DLog.e(TAG, error);

        if (getContext() == null) {
            return;
        }
        inputView.requestFocus();

        if (error != null) {
            try {
                if (error instanceof SyntaxError) {
                    SyntaxError syntaxError = (SyntaxError) error;
                    int rowIndex = syntaxError.getRowIndex();
                    int columnIndex = syntaxError.getColumnIndex();
                    ContentLine row = inputView.getText().getLine(rowIndex);
                    columnIndex = Math.min(columnIndex, row.length());
                    inputView.setSelection(rowIndex, columnIndex);
                    displayErrorMessage(syntaxError.getError());
                } else {
                    displayErrorMessage(ErrorHandler.getErrorMessage(getContext(),error));
                }
            } catch (Exception e) {
                ViewUtils.showMessageDialog(requireContext(), e.getMessage() != null
                        ? e.getMessage() : e.getClass().getName());
            }
        }

        finishCalculating();
    }

}
