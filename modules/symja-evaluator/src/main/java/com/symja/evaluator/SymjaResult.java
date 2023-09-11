package com.symja.evaluator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.matheclipse.core.interfaces.IExpr;

public class SymjaResult {
    @NonNull
    private final IExpr result;
    @Nullable
    private final String stdout;
    @Nullable
    private final String stderr;

    public SymjaResult(@NonNull IExpr result,
                       @Nullable String stdout,
                       @Nullable String stderr) {

        this.result = result;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    @NonNull
    public IExpr getResult() {
        return result;
    }

    @Nullable
    public String getStdout() {
        return stdout;
    }

    @Nullable
    public String getStderr() {
        return stderr;
    }
}
