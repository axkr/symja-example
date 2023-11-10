package com.symja.editor

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import com.symja.common.logging.DLog
import com.symja.editor.SymjaCompletionAdapter.OnItemClickListener
import com.symja.programming.R
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import org.eclipse.tm4e.core.registry.IThemeSource
import kotlin.math.max

class SymjaEditor : CodeEditor {


    var delegate: SymjaEditorDelegate? = null

    private var maxCompletionPopupHeight: Int? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return


        if (attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.SymjaEditor);
                if (typedArray.hasValue(R.styleable.SymjaEditor_lineNumberVisible)) {
                    val lineNumberVisible =
                        typedArray.getBoolean(R.styleable.SymjaEditor_lineNumberVisible, false)
                    isLineNumberEnabled = lineNumberVisible;
                }

                if (typedArray.hasValue(R.styleable.SymjaEditor_maxCompletionPopupHeight)) {
                    maxCompletionPopupHeight = typedArray.getDimensionPixelSize(
                        R.styleable.SymjaEditor_maxCompletionPopupHeight,
                        0
                    );
                }
            } catch (e: Exception) {
                DLog.e(Companion.TAG, e);
            } finally {
                typedArray?.recycle()
            }
        }

        loadThemes(context)

        loadGrammar()
        setupSymjaLanguage()
        setupAutoComplete();
        ensureTextmateTheme()
        switchThemeIfRequired(context, this)


        val font = Typeface.createFromAsset(context.assets, "fonts/JetBrainsMono-Regular.ttf")
        val editor = this;
        editor.typefaceText = font
        editor.typefaceLineNumber = font
        editor.isWordwrap = true


    }

    private fun setupAutoComplete() {
        val symjaEditorAutoCompletionPopup: SymjaEditorAutoCompletionPopup =
            SymjaEditorAutoCompletionPopup(this)

        symjaEditorAutoCompletionPopup.setLayout(SymjaCompletionLayout())

        maxCompletionPopupHeight?.let {
            symjaEditorAutoCompletionPopup.setOverrideMaxHeight(it)
        }

        replaceComponent(
            EditorAutoCompletion::class.java,
            symjaEditorAutoCompletionPopup
        )

        symjaEditorAutoCompletionPopup.symjaCompletionAdapter.setOnItemClickListener(object :
            OnItemClickListener {
            override fun onIconClick(position: Int, completionItem: CompletionItem?) {
                delegate?.onSuggestionIconClicked(position, completionItem)
            }

            override fun onItemClick(position: Int, completionItem: CompletionItem?) {
                symjaEditorAutoCompletionPopup.select(position)
            }
        })
    }

    private fun setupSymjaLanguage() {
        val symjaLanguageProxy = SymjaLanguageProxy(
            context,
            TextMateLanguage.create(
                "source.mathematica",
                true
            )
        )
        setEditorLanguage(symjaLanguageProxy)

    }


    private fun ensureTextmateTheme() {
        val editor = this;
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = editorColorScheme
        }
    }

    private fun loadGrammar() {
        // language
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
    }

    private fun loadThemes(context: Context) {
        //add assets file provider
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                context.assets
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

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        switchThemeIfRequired(context, this)
    }

    private fun switchThemeIfRequired(context: Context, editor: CodeEditor) {
        if ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            if (editor.colorScheme is TextMateColorScheme) {
                ThemeRegistry.getInstance().setTheme("darcula")
            } else {
                editor.colorScheme = SchemeDarcula()
            }
        } else {
            if (editor.colorScheme is TextMateColorScheme) {
                ThemeRegistry.getInstance().setTheme("quietlight")
            } else {
                editor.colorScheme = EditorColorScheme()
            }
        }
        editor.invalidate()
    }

    fun insert(text: String) {
        this.insertText(text, text.length)
    }

    fun setSelection(index: Int) {
        //TODO Implement
    }

    companion object {
        private const val TAG = "SymjaEditor"
    }
}