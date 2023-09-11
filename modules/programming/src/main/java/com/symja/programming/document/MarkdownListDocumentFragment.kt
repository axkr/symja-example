package com.symja.programming.document

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.symja.common.logging.DLog
import com.symja.programming.ProgrammingContract
import com.symja.programming.ProgrammingContract.IDocumentView
import com.symja.programming.R
import com.symja.programming.document.MarkdownListDocumentAdapter.OnDocumentClickListener
import com.symja.programming.document.model.DocumentItem
import com.symja.programming.document.model.DocumentStructureLoader
import com.symja.programming.document.view.MarkdownViewDelegate
import com.symja.programming.document.view.NativeMarkdownView
import com.symja.programming.utils.ApplicationUtils
import com.symja.programming.view.text.SimpleTextWatcher
import java.util.Locale
import java.util.Stack

class MarkdownListDocumentFragment : Fragment(), OnDocumentClickListener, IDocumentView,
    MarkdownViewDelegate {

    private val displayingItemStack = Stack<DocumentItem>()
    private val homeItem = DocumentItem("", "Home", "")

    private var searchView: EditText? = null
    private var viewFlipper: ViewFlipper? = null
    private var documentNavigationView: RecyclerView? = null
    private var documentNavigationContainer: View? = null
    private var listDocumentAdapter: MarkdownListDocumentAdapter? = null
    private var documentNavigationAdapter: DocumentNavigationAdapter = DocumentNavigationAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.symja_prgm_fragment_programming_document, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val documentItems = documentItems
        val context = requireContext()

        // never pop homeItem
        displayingItemStack.push(homeItem);

        listDocumentAdapter = MarkdownListDocumentAdapter(context, documentItems)
        listDocumentAdapter?.setOnDocumentClickListener(this)

        val documentListView = view.findViewById<RecyclerView>(R.id.document_list_view)
        documentListView.setHasFixedSize(false)
        documentListView.isNestedScrollingEnabled = false
        documentListView.layoutManager = LinearLayoutManager(context)
        documentListView.adapter = listDocumentAdapter
        searchView = view.findViewById(R.id.edit_search_view)
        searchView?.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                onQueryTextChange(s.toString())
            }
        })
        if (savedInstanceState != null) {
            val query: String? = savedInstanceState.getString(EXTRA_QUERY)
            if (!query.isNullOrEmpty()) {
                searchView?.setText(query)
            }
        }
        viewFlipper = view.findViewById(R.id.view_flipper2)
        viewFlipper?.displayedChild = 0
        viewFlipper?.setInAnimation(getContext(), android.R.anim.slide_in_left)
        viewFlipper?.setOutAnimation(getContext(), android.R.anim.slide_out_right)

        documentNavigationContainer = view.findViewById(R.id.document_navigation_container)
        documentNavigationContainer?.isVisible = false;

        documentNavigationView = view.findViewById(R.id.document_navigation_view)
        documentNavigationAdapter.setOnItemClickListener { position: Int, documentItem: DocumentItem ->
            switchToPosition(
                position,
                documentItem
            )
        }
        documentNavigationAdapter.submitList(this.displayingItemStack.toMutableList())
        documentNavigationView?.adapter = documentNavigationAdapter

        restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (searchView != null) {
            outState.putString(EXTRA_QUERY, searchView!!.text.toString())
        }
        outState.putSerializable(EXTRA_DISPLAYING_ITEM, ArrayList(displayingItemStack))
    }

    private val documentItems: ArrayList<DocumentItem>
        get() {
            var documentItems: ArrayList<DocumentItem>? = null
            val arguments = arguments
            if (arguments != null && arguments.containsKey(EXTRA_ITEMS)) {
                try {
                    val serializable = arguments.getSerializable(EXTRA_ITEMS) as List<DocumentItem>?
                    if (serializable != null) {
                        documentItems = ArrayList(serializable)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (documentItems == null) {
                documentItems = ArrayList()
            }
            return documentItems
        }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_DISPLAYING_ITEM)) {
            val documentItems =
                savedInstanceState.getSerializable(EXTRA_DISPLAYING_ITEM) as List<DocumentItem>?
            if (documentItems != null) {
                for (documentItem in documentItems) {
                    onDocumentClick(documentItem)
                }
            }
        }
    }

    override fun onDocumentClick(item: DocumentItem) {
        if (activity != null) {
            hideKeyboard(requireActivity())
        }
        pushStack(item);
        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity?)?.supportActionBar
            if (actionBar != null) {
                actionBar.subtitle = item.name
            }
        }
    }

    override fun onUrlClick(title: CharSequence?, url: String) {
        if (context == null || url.isBlank()) {
            return
        }
        ApplicationUtils.openUrl(requireContext(), url)
    }

    private fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setPushAnimation(viewFlipper: ViewFlipper) {
        viewFlipper.setInAnimation(context, R.anim.slide_in_right)
        viewFlipper.setOutAnimation(context, R.anim.slide_out_left)
    }

    private fun setPopAnimation(viewFlipper: ViewFlipper) {
        viewFlipper.setInAnimation(context, R.anim.slide_in_left)
        viewFlipper.setOutAnimation(context, R.anim.slide_out_right)
    }

    private fun onQueryTextChange(newText: String) {
        listDocumentAdapter!!.query(newText)
    }

    override fun setPresenter(presenter: ProgrammingContract.IPresenter) {
    }

    override fun onBackPressed(): Boolean {
        if (popStack()) {
            return true;
        }
        return false
    }

    override fun openDocument(documentItem: DocumentItem) {
        onDocumentClick(documentItem)
    }

    override fun onLinkClick(title: CharSequence?, url: String) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "onLinkClick() called with: title = [$title], url = [$url]")
        }
        if (context == null) {
            return
        }
        if (url.startsWith("https://") || url.startsWith("http://")) {
            onUrlClick(title, url)
            return
        } else if (url.startsWith("#")) { // page navigation
            if (viewFlipper != null) {
                val pageView = viewFlipper!!.getChildAt(viewFlipper!!.childCount - 1)
                val sectionView = pageView.findViewWithTag<View>(
                    "#" + url.lowercase(Locale.getDefault()).replace("\\W".toRegex(), "")
                )
                if (sectionView != null && pageView is ScrollView) {
                    pageView.smoothScrollTo(0, sectionView.top)
                }
            }
            return
        }
        val documentItems = DocumentStructureLoader.loadDocumentStructure(requireContext())
        for (documentItem in documentItems) {
            val assetPath = documentItem.assetPath
            if (assetPath.contains(url) || url.contains(assetPath)) {
                onDocumentClick(documentItem)
                break
            }
        }
    }

    override fun onCopyCodeButtonClicked(v: View?, code: String) {
        ExpressionCopyingDialog.show(context, v, code)
    }

    private fun pushStack(item: DocumentItem) {
        val viewFlipper = this.viewFlipper ?: return;


        setPushAnimation(viewFlipper)
        try {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.symja_prgm_document_layout_markdown_view, viewFlipper, false)
            viewFlipper.addView(view)
            viewFlipper.displayedChild = viewFlipper.childCount - 1
            val markdownView = view.findViewById<NativeMarkdownView>(R.id.markdown_view)
            markdownView.setDelegate(this)
            markdownView.loadMarkdownFromAssets(item.assetPath)
            displayingItemStack.push(item)
            documentNavigationAdapter.submitList(ArrayList(displayingItemStack))
        } catch (e: Exception) {
            if (DLog.DEBUG) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }

        this.documentNavigationContainer?.isVisible = this.displayingItemStack.size > 1;

    }

    private fun popStack(): Boolean {
        val viewFlipper = this.viewFlipper ?: return false;

        if (displayingItemStack.size == 1 || displayingItemStack.isEmpty()) {
            // Home item
            return false;
        }
        displayingItemStack.pop();

        setPopAnimation(viewFlipper)
        viewFlipper.removeViewAt(viewFlipper.childCount - 1)


        this.documentNavigationContainer?.isVisible = this.displayingItemStack.size > 1;

        return true
    }

    private fun switchToPosition(position: Int, documentItem: DocumentItem) {
        val viewFlipper = this.viewFlipper ?: return;

        // remove items after "position"
        // [0,1]
        while (displayingItemStack.size > position + 1) {
            displayingItemStack.pop()
        }
        documentNavigationAdapter.submitList(ArrayList(displayingItemStack))

        setPopAnimation(viewFlipper)
        // remove views after "position"
        while (viewFlipper.childCount > position + 1) {
            viewFlipper.removeViewAt(viewFlipper.childCount - 1)
        }


        this.documentNavigationContainer?.isVisible = this.displayingItemStack.size > 1;

    }

    companion object {
        private const val EXTRA_QUERY = "MarkdownListDocumentFragment.EXTRA_QUERY"
        private const val EXTRA_ITEMS = "MarkdownListDocumentFragment.EXTRA_ITEMS"
        private const val EXTRA_DISPLAYING_ITEM = "MarkdownListDocumentFragment.EXTRA_ITEM_STACK"
        private const val TAG = "MarkdownListDocumentFra"
        fun newInstance(items: ArrayList<DocumentItem?>?): MarkdownListDocumentFragment {
            val args = Bundle()
            val fragment = MarkdownListDocumentFragment()
            args.putSerializable(EXTRA_ITEMS, items)
            fragment.arguments = args
            return fragment
        }
    }
}