package com.symja.app

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.symja.app.editor.TextMateLanguageProxy
import com.symja.app.math.OutputForm
import com.symja.app.math.Symja
import com.symja.app.math.SymjaResult
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import org.eclipse.tm4e.core.registry.IThemeSource
import org.matheclipse.core.convert.AST2Expr
import ru.noties.jlatexmath.JLatexMathView
import java.lang.reflect.Type
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var btnCalc: View? = null
    private lateinit var editor: CodeEditor
    private var tabLayout: TabLayout? = null
    private var viewFlipper: ViewFlipper? = null
    private var resultLabel: TextView? = null
    private var latexLabel: JLatexMathView? = null
    private var errorMessageLabel: TextView? = null
    private var standardMessageLabel: TextView? = null
    private var loadingView: CircularProgressIndicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFlipper = findViewById(R.id.view_flipper)
        tabLayout = findViewById(R.id.tab_layout)
        editor = findViewById(R.id.input_field)
        configInputField()

        resultLabel = findViewById(R.id.result_label)
        resultLabel?.typeface = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")

        latexLabel = findViewById(R.id.latex_view)
        errorMessageLabel = findViewById(R.id.stderr_label)
        standardMessageLabel = findViewById(R.id.stdout_label)
        loadingView = findViewById(R.id.loading_view)
        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                switchTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        btnCalc = findViewById(R.id.btn_calc)
        btnCalc?.setOnClickListener { calculate() }

    }

    private fun configInputField() {
        loadThemes()
        loadGrammar()

        val font = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        editor.setTextSize(14f)
        editor.typefaceText = font
        editor.typefaceLineNumber = font
        editor.isWordwrap = true

        AST2Expr.initialize()
        val language: TextMateLanguage = TextMateLanguage.create("source.mathematica", true)
        val textMateLanguageProxy = TextMateLanguageProxy(language)
        // $ is used for code snippet
        textMateLanguageProxy.setCompleterKeywords(
            AST2Expr.PREDEFINED_SYMBOLS_MAP.map { x -> x.value.replace("$", "") }
            .toSet().toTypedArray())
        editor.setEditorLanguage(textMateLanguageProxy)

        ensureTextmateTheme()
        switchThemeIfRequired(this, editor)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        switchThemeIfRequired(this, editor)
    }

    private fun loadGrammar() {
        // language
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
    }

    private fun loadThemes() {
        //add assets file provider
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                applicationContext.assets
            )
        )


        val themes = arrayOf("darcula", "abyss", "quietlight", "solarized_drak")
        val themeRegistry = ThemeRegistry.getInstance()
        themes.forEach { name ->
            val path = "textmate/$name.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                    ), name
                ).apply {
                    if (name != "quietlight") {
                        isDark = true
                    }
                }
            )
        }

        themeRegistry.setTheme("quietlight")
    }

    private fun ensureTextmateTheme() {
        val editor = editor;
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = editorColorScheme
        }
    }

    private fun calculate() {
        onCalculationStart()
        val input = editor.text.toString()
        executor.execute {
            try {
                val symja = Symja.getInstance()
                val result = symja.eval(input)
                mainThreadHandler.post { onCalculationComplete(result) }
                //Background work here
            } catch (error: Throwable) {
                error.printStackTrace()
                mainThreadHandler.post { onCalculationError(error) }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun onCalculationComplete(result: SymjaResult) {
        onCalculationDone()
        switchTab(RESULT_TAB)
        resultLabel!!.text = OutputForm.toString(result.result)
        latexLabel!!.setLatex(OutputForm.toLatex(result.result))
        errorMessageLabel!!.text = result.stderr
        standardMessageLabel!!.text = result.stdout
    }

    private fun switchTab(index: Int) {
        tabLayout!!.selectTab(tabLayout!!.getTabAt(index))
        viewFlipper!!.displayedChild = index
    }

    private fun onCalculationError(error: Throwable) {
        onCalculationDone()
        switchTab(STDERR_TAB)
        setErrorMessage(error.message)
    }

    private fun onCalculationStart() {
        ViewUtils.hideKeyboard(editor)
        btnCalc!!.isEnabled = false
        loadingView!!.show()
    }

    private fun onCalculationDone() {
        btnCalc!!.isEnabled = true
        loadingView!!.hide()
    }

    private fun setErrorMessage(message: String?) {
        errorMessageLabel!!.text = message
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RESULT_TAB = 0
        private const val LATEX_TAB = RESULT_TAB + 1
        private const val STDOUT_TAB = LATEX_TAB + 1
        private const val STDERR_TAB = STDOUT_TAB + 1
    }
}