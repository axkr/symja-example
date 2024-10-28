package com.symja.evaluator;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.symja.common.logging.DLog;
import com.symja.evaluator.config.SymjaEvaluationConfig;

import org.apfloat.FixedPrecisionApfloatHelper;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.basic.OperationSystem;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.eval.util.WriterOutputStream;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Symja {
    private static final String TAG = "Symja";

    private static final Semaphore semaphore = new Semaphore(1);
    private static final Object lock = new Object();
    private static final long CALCULATION_THREAD_STACK_SIZE_BYTES = 2_097_152;
    private static final AtomicBoolean systemInited = new AtomicBoolean(false);

    public static long calculationTimeOutMillis = 120 * 1000;

    private static Symja instance;

    private final ThreadGroup threadGroup = new ThreadGroup("CalculateThread");
    private final ExprEvaluator exprEvaluator;

    private Symja() {
        this.exprEvaluator = new ExprEvaluator();
    }

    @WorkerThread
    public static Symja getInstance() {
        synchronized (lock) {
            if (instance == null) {
                initSymja();
                instance = new Symja();
            }
        }
        return instance;
    }

    @WorkerThread
    private static void initSymja() {
        if (!systemInited.get()) {
            try {
                if (DLog.DEBUG) {
                    Log.d(TAG, "init called");
                }
                semaphore.acquire();

                Config.DISPLAY_JSFIDDLE_BUTTON = false;
                Config.JAVA_AWT_DESKTOP_AVAILABLE = false;

                F.initSymbols();

                systemInited.set(true);

                if (DLog.DEBUG) {
                    Log.d(TAG, "init done");
                }
            } catch (Exception e) { // Should not throw error
                Log.e(TAG, e.getMessage(), e);
            } finally {
                semaphore.release();
            }
        }
    }

    @WorkerThread
    public SymjaResult eval(IExpr expr) throws Exception {

        final AtomicReference<SymjaResult> resultRef = new AtomicReference<>();
        final AtomicReference<Exception> exceptionRef = new AtomicReference<>();
        final AtomicReference<Error> errorRef = new AtomicReference<>();
        // ensure that interrupt is reset
        OperationSystem.setInterrupt(false);

        final IExpr finalExpr = expr;

        try {
            long precision = finalExpr.determinePrecision(true);
            EvalEngine evalEngine = exprEvaluator.getEvalEngine();
            FixedPrecisionApfloatHelper fixedPrecisionApfloatHelper = evalEngine.setNumericPrecision(precision);
            if (DLog.DEBUG) {
                DLog.d(TAG, "fixedPrecisionApfloatHelper = " + fixedPrecisionApfloatHelper);
            }

            final StringWriter outWriter = new StringWriter();
            WriterOutputStream stdOut = new WriterOutputStream(outWriter);
            PrintStream outs = new PrintStream(stdOut);
            final StringWriter errorWriter = new StringWriter();
            WriterOutputStream stdErr = new WriterOutputStream(errorWriter);
            PrintStream errors = new PrintStream(stdErr);

            // example: output from Print() function goes into output stream
            evalEngine.setOutPrintStream(outs);
            // example: error messages (like incorrect number of args) goes into error stream
            evalEngine.setErrorPrintStream(errors);

            EvalEngine[] engineRef = new EvalEngine[]{evalEngine};
            IExpr result = ExprEvaluator.evalTryCatch(finalExpr, engineRef);
            resultRef.set(new SymjaResult(result, outWriter.toString(), errorWriter.toString()));
        } catch (Exception e) {
            exceptionRef.set(e);
        } catch (Error error) {
            errorRef.set(error);
        }

        if (exceptionRef.get() != null) {
            throw exceptionRef.get();
        }
        if (errorRef.get() != null) {
            throw errorRef.get();
        }
        SymjaResult result = resultRef.get();
        return result;
    }

    public SymjaResult eval(String input) throws Exception {
        return eval(exprEvaluator.parse(input));
    }

    public SymjaResult evaluate(String expression, SymjaEvaluationConfig config) throws Exception {
        // TODO: evaluate with config
        return eval(expression);
    }

    @NonNull
    public ExprEvaluator getExprEvaluator() {
        return exprEvaluator;
    }

    @NonNull
    public IExpr parse(@NonNull String input) {
        return exprEvaluator.parse(input);
    }
}
