package com.symja.app;

import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.interfaces.IExpr;

public class OutputForm {
    public static String toString(IExpr result) {
        if (result == null) {
            return "";
        }
        try {
            OutputFormFactory outputFormFactory = OutputFormFactory.get(true, false);
            return outputFormFactory.toString(result);
        } catch (Exception e) {
            return "Error";
        }
    }
}
