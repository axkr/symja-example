package com.symja.programming.symjatalk;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.programming.settings.CalculatorSettings;
import com.symja.programming.symjatalk.api.SymjaTalkRequest;
import com.symja.programming.symjatalk.api.SymjaTalkRequestProcessor;
import com.symja.programming.symjatalk.api.SymjaTalkResult;

import java.lang.ref.WeakReference;

public class SymjaTalkPresenter implements SymjaTalkContract.IPresenter {

    private final Context context;
    private final CalculatorSettings settings;

    public SymjaTalkPresenter(Context context) {
        this.context = context;
        this.settings = CalculatorSettings.newInstance(context);
    }

    @NonNull
    @Override
    public AsyncTask<Void, Void, SymjaTalkResult> createRequestTask(SymjaTalkContract.ResultCallback callback,
                                                                    SymjaTalkRequest input) {
        @Nullable String host = settings.getSymjaServer();
        boolean offlineMode = settings.isUseSymjaTalkOffline();
        return new CalculateTask(host, offlineMode, callback, input);
    }

    static class CalculateTask extends AsyncTask<Void, Void, SymjaTalkResult> {

        @Nullable
        private final String symjaHost;
        private final boolean offlineMode;
        private final SymjaTalkRequest input;
        private final WeakReference<SymjaTalkContract.ResultCallback> resultCallback;
        private Throwable error;

        @SuppressWarnings("deprecation")
        CalculateTask(@Nullable String symjaHost, boolean offlineMode, @Nullable SymjaTalkContract.ResultCallback resultCallback,
                      @NonNull SymjaTalkRequest input) {
            this.symjaHost = symjaHost;
            this.offlineMode = offlineMode;
            this.input = input;
            this.resultCallback = new WeakReference<>(resultCallback);
        }

        @Override
        protected SymjaTalkResult doInBackground(Void... voids) {
            try {
                SymjaTalkRequestProcessor processor = new SymjaTalkRequestProcessor(symjaHost, offlineMode);
                return processor.startRequest(input);
            } catch (Exception | Error e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(SymjaTalkResult result) {
            super.onPostExecute(result);
            if (isCancelled()) {
                return;
            }
            if (error != null) {
                SymjaTalkContract.ResultCallback resultCallback = this.resultCallback.get();
                if (resultCallback != null) {
                    resultCallback.onError(error, input);
                }
            } else {
                SymjaTalkContract.ResultCallback resultCallback = this.resultCallback.get();
                if (resultCallback != null) {
                    resultCallback.onSuccess(result, input);
                }
            }

        }

    }
}
