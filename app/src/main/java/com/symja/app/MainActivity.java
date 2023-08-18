package com.symja.app;

import static com.symja.app.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.symja.app.math.OutputForm;
import com.symja.app.math.Symja;
import com.symja.app.math.SymjaResult;

import org.eclipse.tm4e.core.registry.IThemeSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
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

    private CodeEditor inputField;

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
        configInputField();

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

    private void configInputField() {
        Typeface font = Typeface.createFromAsset(getAssets(), "JetBrainsMono-Regular.ttf");
        inputField.setTextSize(14);
        inputField.setTypefaceText(font);
        inputField.setTypefaceLineNumber(font);
        inputField.setWordwrap(true);

        // theme
        //add assets file provider
        FileProviderRegistry.getInstance().addFileProvider(
                new AssetsFileResolver(
                        getApplicationContext().getAssets()
                )
        );

        List<String> themes = Arrays.asList("darcula", "abyss", "quietlight", "solarized_drak");
        ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
        themes.forEach(name -> {
            String path = "textmate/" + name + ".json";
            IThemeSource themeSource = IThemeSource.fromInputStream(Objects.requireNonNull(FileProviderRegistry.getInstance().tryGetInputStream(path)), path, null);
            ThemeModel themeModel = new ThemeModel(themeSource, name);

            if (!Objects.equals(name, "quietlight")) {
                themeModel.setDark(true);
            }
            try {
                themeRegistry.loadTheme(themeModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        themeRegistry.setTheme("quietlight");

        EditorColorScheme editorColorScheme = inputField.getColorScheme();
        if (!(editorColorScheme instanceof TextMateColorScheme)) {
            try {
                editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance());
                inputField.setColorScheme(editorColorScheme);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // language
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json");

        inputField.setText("Solve(Xor(a, b, c, d) && (a || b) && ! (c || d), {a, b, c, d}, Booleans)\n" +
                "{{a->False,b->True,c->False,d->False},{a->True,b->False,c->False,d->False}}");
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
            } catch (Throwable error) {
                error.printStackTrace();
                mainThreadHandler.post(() -> onCalculationError(error));
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void onCalculationComplete(@NonNull SymjaResult result) {
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

    private void onCalculationError(@NonNull Throwable error) {

        onCalculationDone();
        switchTab(STDERR_TAB);
        setErrorMessage(error.getMessage());
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