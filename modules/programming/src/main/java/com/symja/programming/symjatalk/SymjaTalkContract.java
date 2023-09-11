package com.symja.programming.symjatalk;

import android.os.AsyncTask;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.programming.symjatalk.api.SymjaTalkRequest;
import com.symja.programming.symjatalk.api.SymjaTalkResult;


public class SymjaTalkContract {

    public interface IPresenter {

        @NonNull
        AsyncTask<Void, Void, SymjaTalkResult> createRequestTask(ResultCallback callback,
                                                                 SymjaTalkRequest input);

    }

    public interface IConsoleView {

        void setPresenter(IPresenter presenter);

    }

    public interface ResultCallback {

        @MainThread
        void onSuccess(@NonNull SymjaTalkResult result, SymjaTalkRequest request);

        @MainThread
        void onError(@Nullable Throwable error, SymjaTalkRequest request);
    }


}
