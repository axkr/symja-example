package com.symja.app;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    static {
        LoggerFix.fix();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            F.initSymbols();
            ExprEvaluator exprEvaluator = new ExprEvaluator();
            Log.d(TAG, String.valueOf(exprEvaluator.eval("Sin(x)+Cos(x)")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}