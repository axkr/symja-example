package com.symja.app

import android.annotation.SuppressLint
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
import com.symja.editor.SymjaEditor
import com.symja.evaluator.OutputForm
import com.symja.evaluator.Symja
import com.symja.evaluator.SymjaResult
import ru.noties.jlatexmath.JLatexMathView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var btnCalc: View? = null
    private lateinit var editor: SymjaEditor
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
        resultLabel?.typeface = Typeface.createFromAsset(assets, "fonts/JetBrainsMono-Regular.ttf")

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
        editor.setTextSize(14f)
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