package com.symja.programming.symjatalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.duy.common.utils.DLog;
import com.google.common.collect.Lists;
import com.symja.common.analyst.AppAnalytics;
import com.symja.common.analyst.AppAnalyticsEvents;
import com.symja.common.datastrcture.Data;
import com.symja.programming.BaseProgrammingFragment;
import com.symja.programming.ProgrammingContract;
import com.symja.programming.R;
import com.symja.programming.console.ProgrammingConsoleDocument;
import com.symja.programming.console.ProgrammingDocumentManager;
import com.symja.programming.console.ProgrammingResultAdapter;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.symjatalk.api.SymjaFormat;
import com.symja.programming.symjatalk.api.SymjaTalkRequest;
import com.symja.programming.symjatalk.api.SymjaTalkResult;
import com.symja.programming.utils.ViewUtils;

import org.matheclipse.parser.client.SyntaxError;

import java.io.IOException;
import java.util.ArrayList;

import io.github.rosemoe.sora.text.ContentLine;

public class SymjaTalkFragment extends BaseProgrammingFragment implements
        SymjaTalkContract.ResultCallback,
        SymjaTalkContract.IConsoleView {

    private static final String TAG = "SymjaTalkFragment";
    private static final String EXTRA_INPUT = "SymjaTalkFragment.input";
    private static final String EXTRA_INPUT_DATA = "SymjaTalkFragment.input_data";

    private static final String defaultDocumentName = "SymjaTalkWorkspace.json";

    @Nullable
    private SymjaTalkContract.IPresenter presenter;
    private ProgrammingDocumentManager documentManager;
    private ProgrammingConsoleDocument editingDocument = new ProgrammingConsoleDocument("SymjaTalk");
    private ProgrammingResultAdapter programmingResultAdapter;

    @Nullable
    private TextView txtLoadingMessage;
    private boolean expressionDetailMode = false;
    private Data input;

    @NonNull
    public static SymjaTalkFragment newInstance(@Nullable Data input) {

        Bundle args = new Bundle();
        if (input != null) {
            args.putSerializable(EXTRA_INPUT_DATA, input);
        }

        SymjaTalkFragment fragment = new SymjaTalkFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.symja_prgm_fragment_symja_talk, container, false);
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
        AppAnalytics.getInstance(getContext()).logEvent(AppAnalyticsEvents.symjaTalkClickSubmit);

        saveCurrentDocument();
        saveCurrentInput();
        prepareUIForCalculating();

        SymjaTalkRequest request = new SymjaTalkRequest(input,
                Lists.newArrayList(
                        SymjaFormat.PLAIN_TEXT,
                        SymjaFormat.LATEX,
                        SymjaFormat.SYMJA));
        performCalculate(request);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        loadEditingDocument();

        super.onViewCreated(view, savedInstanceState);

        txtLoadingMessage = view.findViewById(R.id.txt_loading_message);
        btnRun.setText(R.string.symja_prgm_button_submit);
        loadSavedInput();
        if (presenter == null) {
            setPresenter(new SymjaTalkPresenter(getContext()));
        }

        if (expressionDetailMode) {
            inputView.setText(input.getValue());
            clickRun();
        }
    }

    private void loadSavedInput() {
        if (!expressionDetailMode) {
            inputView.setText(settings.getString(EXTRA_INPUT, ""));
        }
    }

    private void saveCurrentInput() {
        if (!expressionDetailMode) {
            settings.putString(EXTRA_INPUT, inputView.getText().toString());
        }
    }

    private void loadEditingDocument() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(EXTRA_INPUT_DATA)) {
                try {
                    this.input = (Data) arguments.getSerializable(EXTRA_INPUT_DATA);
                    this.expressionDetailMode = true;
                } catch (Exception e) {
                    if (DLog.DEBUG) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }

        //noinspection ConstantConditions
        documentManager = new ProgrammingDocumentManager(getContext());
        if (expressionDetailMode) {
            this.editingDocument = new ProgrammingConsoleDocument("ExpressionDetails.json");
        } else {
            try {
                this.editingDocument = documentManager.readDocument(defaultDocumentName);
            } catch (Exception e) {
                try {
                    this.editingDocument = documentManager.createDocument(defaultDocumentName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    this.editingDocument = new ProgrammingConsoleDocument(defaultDocumentName);
                }
            }

        }
        // Todo: add example for users
        // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        // boolean firstLaunch = preferences.getBoolean("symjatalk_first_launch", true);
        // if (firstLaunch && (BuildConfig.DEBUG && editingDocument.isEmpty())) {
        //     try {
        //         String content = IOUtils.toString(getContext().getAssets().open("console/Document1.json"));
        //         JSONObject jsonObject = new JSONObject(content);
        //         ProgrammingConsoleDocument examples = new ProgrammingConsoleDocument(jsonObject.toMap());
        //         this.editingDocument.addAll(examples);
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }
    }

    private void saveCurrentDocument() {
        if (!expressionDetailMode) {
            try {
                documentManager.saveDocument(editingDocument);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareUIForCalculating() {
        btnRun.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        ViewUtils.showView(txtLoadingMessage);
    }

    private void performCalculate(@NonNull final SymjaTalkRequest input) {
        if (presenter != null) {
            AsyncTask<Void, Void, SymjaTalkResult> task = presenter.createRequestTask(
                    this, input);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateUICalculateComplete(@Nullable ArrayList<CalculationItem> items) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "updateUICalculateComplete() called with: items =");
            if (items != null) {
                for (CalculationItem item : items) {
                    DLog.d(TAG, item + "");
                }
            }
        }
        finishCalculating();
        if (items != null) {
            if (!items.isEmpty()) {
                editingDocument.addAll(0, items);
                programmingResultAdapter.notifyItemRangeInserted(0, items.size());

                while (editingDocument.size() > 100) {
                    editingDocument.remove(editingDocument.size() - 1);
                    programmingResultAdapter.notifyItemRemoved(editingDocument.size() - 1);
                }

                listResultView.scrollToPosition(0);
                ViewUtils.hideKeyboard(inputView.getContext(), inputView);
            } else {
                if (getContext() != null) {
                    ViewUtils.showMessageDialog(getContext(), getString(R.string.symja_prgm_message_empty_result));
                }
            }
        }
    }

    private void finishCalculating() {
        if (DLog.DEBUG) {
            DLog.d(TAG, "finishCalculating() called");
        }
        progressBar.setVisibility(View.GONE);
        btnRun.setVisibility(View.VISIBLE);
        ViewUtils.hideView(txtLoadingMessage);
    }

    @Override
    public void setPresenter(@Nullable SymjaTalkContract.IPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onSuccess(@NonNull SymjaTalkResult result, SymjaTalkRequest request) {
        ArrayList<CalculationItem> calculationItems = result.getCalculationItems();
        if (getContext() == null) {
            return;
        }
        updateUICalculateComplete(calculationItems);
    }

    @Override
    public void onError(@Nullable Throwable error, SymjaTalkRequest request) {
        DLog.e(error);
        if (getContext() == null) {
            return;
        }
        if (error != null) {
            if (error instanceof SyntaxError) {
                SyntaxError syntaxError = (SyntaxError) error;
                int rowIndex = syntaxError.getRowIndex();
                int columnIndex = syntaxError.getColumnIndex();
                ContentLine row = inputView.getText().getLine(rowIndex);
                columnIndex = Math.min(columnIndex, row.length());
                inputView.setSelection(rowIndex, columnIndex);
                displayErrorMessage(syntaxError.getError());
            } else {
                displayErrorMessage(error.getMessage());
            }
        }
        finishCalculating();
    }

}
