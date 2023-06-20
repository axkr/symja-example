package com.symja.app;

import static com.symja.app.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.symja.app.math.OutputForm;
import com.symja.app.math.Symja;
import com.symja.app.math.SymjaResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.noties.jlatexmath.JLatexMathView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int RESULT_TAB = 0;
    private static final int LATEX_TAB = RESULT_TAB + 1;
    private static final int STDOUT_TAB = LATEX_TAB + 1;
    private static final int STDERR_TAB = STDOUT_TAB + 1;

    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executor;
    private View btnCalc;
    private EditText inputField;
    private TabLayout tabLayout;
    private ViewFlipper viewFlipper;
    private TextView resultLabel;
    private JLatexMathView latexLabel;
    private TextView errorMessageLabel;
    private TextView standardMessageLabel;
    private CircularProgressIndicator loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();

        viewFlipper = findViewById(R.id.view_flipper);
        tabLayout = findViewById(R.id.tab_layout);
        inputField = findViewById(R.id.input_field);
        resultLabel = findViewById(R.id.result_label);
        latexLabel = findViewById(R.id.latex_view);
        errorMessageLabel = findViewById(R.id.stderr_label);
        standardMessageLabel = findViewById(R.id.stdout_label);
        loadingView = findViewById(R.id.loading_view);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        btnCalc = findViewById(R.id.btn_calc);
        btnCalc.setOnClickListener(v -> calculate());
    }

    private void calculate() {
        onCalculationStart();

        String input = inputField.getText().toString();
        executor.execute(() -> {
            try {
                Symja symja = Symja.getInstance();
                SymjaResult result = symja.eval(input);
                mainThreadHandler.post(() -> onCalculationComplete(result));
                //Background work here
            } catch (Throwable throwable) {
                mainThreadHandler.post(() -> onCalculationError(throwable));
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void onCalculationComplete(SymjaResult result) {
        onCalculationDone();

        switchTab(RESULT_TAB);

        resultLabel.setText(OutputForm.toString(result.getResult()));
        latexLabel.setLatex(OutputForm.toLatex(result.getResult()));

        errorMessageLabel.setText(result.getStderr());
        standardMessageLabel.setText(result.getStdout());
    }

    private void switchTab(int index) {
        tabLayout.selectTab(tabLayout.getTabAt(index));
        viewFlipper.setDisplayedChild(index);
    }

    private void onCalculationError(@NonNull Throwable throwable) {
        onCalculationDone();
        switchTab(STDERR_TAB);
        setErrorMessage(throwable.getMessage());
    }

    private void onCalculationStart() {
        hideKeyboard(this.inputField);
        btnCalc.setEnabled(false);
        loadingView.show();
    }

    private void onCalculationDone() {
        btnCalc.setEnabled(true);
        loadingView.hide();

    }

    private void setErrorMessage(String message) {
        errorMessageLabel.setText(message);
    }


}