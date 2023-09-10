package com.symja.evaluator;

import org.jetbrains.annotations.Nullable;
import org.matheclipse.core.interfaces.IExpr;

public class SymjaResult {
    @Nullable
    private final IExpr result;
    @Nullable
    private final String stdout;
    @Nullable
    private final String stderr;

    public SymjaResult(IExpr result, String stdout, String stderr) {

        this.result = result;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public IExpr getResult() {
        return result;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }
}
