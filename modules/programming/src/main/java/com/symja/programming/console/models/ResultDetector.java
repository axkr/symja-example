package com.symja.programming.console.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.symja.common.datastrcture.Data;
import com.symja.common.logging.DLog;
import com.symja.evaluator.OutputForm;
import com.symja.evaluator.Symja;
import com.symja.evaluator.SymjaResult;

import org.apache.commons.io.FileUtils;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.TeXUtilities;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.expression.StringX;
import org.matheclipse.core.expression.data.GraphExpr;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.IStringX;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class ResultDetector {

    private static final String TAG = "ResultDetector";

    @NonNull
    public static CalculationItem resolve(IExpr res, String inputExpression) {
        SymjaResult simpleResult = new SymjaResult(res, null, null);
        return resolve(simpleResult, inputExpression);
    }

    @NonNull
    public static CalculationItem resolve(SymjaResult res, String inputExpression) {
        if (DLog.DEBUG) {
            Log.d(TAG, "resolve: result = " + res);
        }
        try {
            return resolveInternal(inputExpression, res);
        } catch (Exception e) {
            e.printStackTrace();

            String message = e.getMessage();
            CalculationItem calculationItem = new CalculationItem(inputExpression,
                    null,
                    Data.Format.TEXT_PLAIN,
                    message != null ? message : "Error");
            calculationItem.setStdErr(message);
            return calculationItem;
        }
    }

    @NonNull
    private static CalculationItem resolveInternal(String inputExpression, @NonNull SymjaResult res) throws IOException {
        IExpr result = res.getResult();
        Symja mathEvaluator = Symja.getInstance();
        EvalEngine engine = mathEvaluator.getExprEvaluator().getEvalEngine();

        String symjaExpr = OutputForm.toString(result);

        CalculationItem item;
        if (result.head().equals(F.Graph) && result instanceof GraphExpr) {

            item = makeGraphResult(inputExpression, symjaExpr, (GraphExpr<?>) result);

        } else if (result.head().equals(F.Graphics)) {

            item = makeGraphicsResult(inputExpression, symjaExpr, (IExpr) result);

        } else if (result.isAST(F.JSFormData, 3)) {

            item = makeJsFormResult(inputExpression, symjaExpr, result);

        } else if (result.isString()) {
            item = makeStringResult(inputExpression, symjaExpr, (StringX) result);

        } else {

            item = makeTeXResult(engine, inputExpression, symjaExpr, result);
        }

        item.setStdOut(res.getStdout());
        item.setStdErr(res.getStderr());

        return item;
    }

    @NonNull
    private static CalculationItem makeGraphicsResult(String inputExpression, String symjaExpr, IExpr result) throws IOException {
        String html = createGraphicsHtml(result);
        return new CalculationItem(inputExpression, symjaExpr, Data.Format.HTML, html != null ? html : "");
    }


    /**
     * Using code editor to highlight text in specific mode
     */
    @NonNull
    private static CalculationItem makeStringResult(String inputExpression,
                                                    String symjaExpr, @NonNull StringX result) {
        Data.Format type;
        switch (result.getMimeType()) {
            case IStringX.TEXT_HTML:
                type = Data.Format.TEXT_HTML;
                break;
            case IStringX.TEXT_MATHML:
                type = Data.Format.TEXT_MATHML;
                break;
            case IStringX.TEXT_LATEX:
                type = Data.Format.TEXT_LATEX;
                break;
            case IStringX.APPLICATION_JAVA:
                type = Data.Format.TEXT_APPLICATION_JAVA;
                break;
            case IStringX.APPLICATION_JAVASCRIPT:
                type = Data.Format.TEXT_APPLICATION_JAVASCRIPT;
                break;
            case IStringX.APPLICATION_SYMJA:
                type = Data.Format.TEXT_APPLICATION_SYMJA;
                break;
            case IStringX.TEXT_JSON:
                type = Data.Format.TEXT_APPLICATION_JSON;
                break;
            case IStringX.TEXT_PLAIN:
            default:
                type = Data.Format.TEXT_PLAIN;
        }
        return new CalculationItem(inputExpression, symjaExpr,
                type, result.toString());
    }

    @NonNull
    public static CalculationItem makeTeXResult(EvalEngine engine, String inputExpression,
                                                String symjaExpr, IExpr result) {
        TeXUtilities teXUtilities = new TeXUtilities(engine,
                engine.isRelaxedSyntax());
        StringWriter latex = new StringWriter();
        if (teXUtilities.toTeX(result, latex)) {
            CalculationItem calculationItem = new CalculationItem(inputExpression, symjaExpr,
                    Data.Format.LATEX, latex.toString());
            calculationItem.addData(Data.text(symjaExpr));
            return calculationItem;
        } else {
            return new CalculationItem(inputExpression, symjaExpr,
                    Data.Format.TEXT_PLAIN, symjaExpr);
        }
    }

    @NonNull
    private static CalculationItem makeJsFormResult(String inputExpression,
                                                    String symjaExpr, IExpr result) {
        String html = createGraphicsHtml(result);
        return new CalculationItem(inputExpression, symjaExpr, Data.Format.HTML, html != null ? html : "");
    }

    @NonNull
    private static CalculationItem makeGraphResult(String inputExpression,
                                                   String symjaExpr, GraphExpr<?> result) {
        String html = createGraphicsHtml(result);
        return new CalculationItem(inputExpression, symjaExpr, Data.Format.HTML, html != null ? html : "");
    }

    @Nullable
    private static String createGraphicsHtml(IExpr result) {
        @Nullable String content = F.showGraphic(result);
        if (content == null) {
            return null;
        }
        try {
            if (new File(content).exists()) {
                File file = new File(content);
                content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            DLog.w(e);
        }
        return content;
    }
}
