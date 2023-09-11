package com.symja.programming;

import android.os.AsyncTask;
import android.widget.ViewFlipper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.symja.common.logging.DLog;
import com.symja.evaluator.Symja;
import com.symja.evaluator.SymjaResult;
import com.symja.evaluator.config.EvaluationConfig;
import com.symja.programming.document.model.DocumentItem;
import com.symja.programming.symjatalk.SymjaTalkContract;

import java.lang.ref.WeakReference;

public class ProgrammingPresenter implements ProgrammingContract.IPresenter {
    @Nullable
    private ViewFlipper viewFlipper;
    @Nullable
    private TabLayout navigationView;
    @Nullable
    private ProgrammingContract.IConsoleView consoleView;
    @Nullable
    private SymjaTalkContract.IConsoleView symjaTalkView;
    @Nullable
    private ProgrammingContract.IDocumentView documentView;

    public ProgrammingPresenter() {

    }

    public ProgrammingPresenter(@Nullable ViewFlipper viewFlipper, @Nullable TabLayout navigationView) {
        this.viewFlipper = viewFlipper;
        this.navigationView = navigationView;
    }

    @Nullable
    public ProgrammingContract.IConsoleView getConsoleView() {
        return consoleView;
    }

    @Override
    public void setConsoleView(@Nullable ProgrammingContract.IConsoleView consoleView) {
        this.consoleView = consoleView;
        if (consoleView != null) {
            consoleView.setPresenter(this);
        }
    }

    public void setSymjaTalkView(@Nullable SymjaTalkContract.IConsoleView symjaTalkView) {
        this.symjaTalkView = symjaTalkView;
    }

    @Nullable
    public ProgrammingContract.IDocumentView getDocumentView() {
        return documentView;
    }

    @Override
    public void setDocumentView(@Nullable ProgrammingContract.IDocumentView documentView) {
        this.documentView = documentView;
        if (documentView != null) {
            documentView.setPresenter(this);
        }
    }

    @NonNull
    @Override
    public AsyncTask<Void, Void, SymjaResult> createCalculateTask(@Nullable ResultCallback callback, @NonNull String input, EvaluationConfig config) {
        return new CalculateTask(callback, input, config);
    }

    @Override
    public boolean openDocument(@NonNull DocumentItem documentItem) {
        if (navigationView != null && navigationView.getTabCount() >= 3) {
            TabLayout.Tab tab = navigationView.getTabAt(2);
            navigationView.selectTab(tab);
        }
        if (documentView != null) {
            documentView.openDocument(documentItem);
            return true;
        }
        return false;
    }

    public boolean onBackPressed() {
        if (viewFlipper != null) {
            switch (viewFlipper.getDisplayedChild()) {
                case 0:
                    return consoleView != null && consoleView.onBackPressed();
                case 1:
                    return symjaTalkView != null && symjaTalkView.onBackPressed();

                case 2:
                    if (documentView != null && documentView.onBackPressed()) {
                        return true;
                    } else {
                        selectConsoleView();
                        return true;
                    }
            }
        }
        return false;
    }

    private void selectConsoleView() {
        // back to console view
        if (navigationView != null) {
            navigationView.selectTab(navigationView.getTabAt(0));
        }
    }

    public interface ResultCallback {

        @MainThread
        void onSuccess(@NonNull SymjaResult result, String expression);

        @MainThread
        void onError(@Nullable Throwable error, String expression);
    }

    static class CalculateTask extends AsyncTask<Void, Void, SymjaResult> {
        private static final String TAG = "CalculateTask";

        private final String expression;
        private final WeakReference<ResultCallback> resultCallback;
        private final EvaluationConfig config;
        private Throwable error;

        @SuppressWarnings("deprecation")
        CalculateTask(@Nullable ResultCallback resultCallback, String expression, EvaluationConfig config) {
            this.expression = expression;
            this.resultCallback = new WeakReference<>(resultCallback);
            this.config = config;
        }

        @Override
        protected SymjaResult doInBackground(Void... voids) {
            try {
                Symja evaluator = Symja.getInstance();
                //avoid framework error when calculating, use MathEvaluator
                return evaluator.evaluate(expression, config);
            } catch (Exception | Error e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(SymjaResult iExpr) {
            super.onPostExecute(iExpr);
            DLog.d(TAG, "onPostExecute() called with: iExpr = [" + iExpr + "]");
            if (isCancelled()) {
                return;
            }
            if (error != null) {
                ResultCallback resultCallback = this.resultCallback.get();
                if (resultCallback != null) {
                    resultCallback.onError(error, expression);
                }
            } else {
                ResultCallback resultCallback = this.resultCallback.get();
                if (resultCallback != null) {
                    resultCallback.onSuccess(iExpr, expression);
                }
            }

        }


    }
}
