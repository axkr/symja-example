package com.symja.programming;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.evaluator.SymjaResult;
import com.symja.evaluator.config.SymjaEvaluationConfig;
import com.symja.programming.document.model.DocumentItem;

public class ProgrammingContract {

    public interface IPresenter {

        void setConsoleView(@Nullable IConsoleView consoleView);

        void setDocumentView(@Nullable IDocumentView documentView);

        @NonNull
        AsyncTask<Void, Void, SymjaResult> createCalculateTask(ProgrammingPresenter.ResultCallback callback,
                                                               String input, SymjaEvaluationConfig config);

        /**
         * @return true if the presenter can display document for documentItem
         */
        boolean openDocument(@NonNull DocumentItem documentItem);
    }

    public interface IDocumentView {

        void setPresenter(IPresenter presenter);

        boolean onBackPressed();

        void openDocument(DocumentItem documentItem);
    }

    public interface IConsoleView {
        void setPresenter(IPresenter presenter);

        boolean onBackPressed();
    }

}
