package com.symja.evaluator;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.form.tex.TeXFormFactory;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.ParserConfig;

public class OutputForm {

    public static String toString(IExpr result) {
        if (result == null) {
            return "";
        }
        try {
            OutputFormFactory outputFormFactory = OutputFormFactory.get(ParserConfig.PARSER_USE_LOWERCASE_SYMBOLS, false);
            String string = outputFormFactory.toString(result);

            if (string.length() > Config.MAX_OUTPUT_SIZE) {
                return "Error: Max output size " + Config.MAX_OUTPUT_SIZE + " characters exceeded";
            }

            return string;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String toLatex(IExpr expr, int exponentFigures, int significantFigures) {
        if (expr == null) {
            return "";
        }
        try {
            TeXFormFactory teXFormFactory = new TeXFormFactory(exponentFigures, significantFigures, " \\cdot ");
            StringBuilder buffer = new StringBuilder();
            if (teXFormFactory.convert(buffer, expr)) {
                return buffer.toString();
            }

            if (buffer.length() > Config.MAX_OUTPUT_SIZE) {
                return "\\text{Error: Max output size " + Config.MAX_OUTPUT_SIZE + " characters exceeded}";
            }

            return "\\text{Error}";
        } catch (Exception e) {
            return "\\text{Error: " + e.getMessage() + "}";
        }
    }
}
