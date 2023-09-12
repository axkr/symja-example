package com.symja.programming;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duy.common.utils.DLog;
import com.duy.ide.editor.view.CodeEditor;
import com.symja.common.analyst.AppAnalytics;
import com.symja.common.analyst.AppAnalyticsEvents;
import com.symja.common.android.ClipboardCompat;
import com.symja.editor.SymjaEditor;
import com.symja.editor.SymjaEditorAutoCompletion;
import com.symja.programming.autocomplete.FunctionSuggestionAdapter;
import com.symja.programming.autocomplete.SuggestionItem;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.ProgrammingResultAdapter;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.document.MarkdownDocumentActivity;
import com.symja.programming.document.model.DocumentItem;
import com.symja.programming.document.model.DocumentStructureLoader;
import com.symja.programming.settings.CalculatorSettings;
import com.symja.programming.settings.IProgrammingSettings;
import com.symja.programming.utils.ViewUtils;
import com.symja.programming.view.dragbutton.DragButton;
import com.symja.programming.view.dragbutton.DragButtonUtils;
import com.symja.programming.view.dragbutton.DragDirection;
import com.symja.programming.view.dragbutton.DragListener;
import com.symja.programming.view.popupmenu.AutoCloseablePopupMenu;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.matheclipse.parser.client.operator.ASTNodeFactory;
import org.matheclipse.parser.client.operator.Operator;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;


public abstract class BaseProgrammingFragment extends Fragment implements DragListener,
        FunctionSuggestionAdapter.OnSuggestionClickListener, OnProgrammingItemClickListener {
    private static final int RC_OPEN_FILE = 1232;
    private static final int RC_REQUEST_PERMISSION = 4444;
    private static final int RC_CHANGE_EDITOR_THEME = 101;

    protected TextView btnRun;
    protected View progressBar;

    protected SymjaEditor inputView;
    protected RecyclerView listResultView;

    protected IProgrammingSettings settings;
    @Nullable
    private DragButton dragButtonPreview;
    @Nullable
    private View containerDragHint;
    private View containerInput;

    private MutableLiveData<Boolean> inputExpanded = new MutableLiveData<>(true);

    private void setupViews(@NonNull View view) {

        containerInput = view.findViewById(R.id.container_input);

        Context context = inputView.getContext();

        List<SuggestionItem> suggestionItems =
                DocumentStructureLoader.loadDocumentStructure(context)
                        .stream().map(x -> new SuggestionItem(x.getAssetPath(), x.getName(), x.getDescription()))
                        .collect(Collectors.toList());
        Map<String, Operator> identifier2OperatorMap = new ASTNodeFactory(true).getIdentifier2OperatorMap();
        for (Operator value : identifier2OperatorMap.values()) {
            String assetPath = suggestionItems.stream()
                    .filter(x -> x.getName().equals(value.getFunctionName()))
                    .map(SuggestionItem::getAssetPath)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            suggestionItems.add(new SuggestionItem(assetPath, value.getOperatorString(), value.getFunctionName()));
        }

        FunctionSuggestionAdapter suggestAdapter = new FunctionSuggestionAdapter(context, android.R.layout.simple_list_item_1, suggestionItems);
        suggestAdapter.setOnSuggestionListener(this);
        // TODO: replace with symja editor inputView.setAdapter(suggestAdapter);
        // TODO: replace with symja editor inputView.setThreshold(2);
        // TODO: replace with symja editor inputView.getDocument().setMode("symja");

        // apply theme
        // TODO: replace with symja editor  changeTheme(Preferences.getInstance(getContext()).getEditorTheme(), view);
    }

    private void addViewEvents(@NotNull View view) {
        btnRun.setOnClickListener(v -> clickRun());
//        view.findViewById(R.id.btn_clear).setOnClickListener(v -> {
//            final Context context = requireContext();
//            if (inputView.getText().length() == 0) {
//                return;
//            }
//            ViewUtils.showConfirmationDialog(context, getString(R.string.clear_all),
//                    aBoolean -> {
//                        if (aBoolean) {
//                            inputView.setText("");
//                            inputView.requestFocus();
//                            ViewUtils.showKeyboard(context, inputView);
//                        }
//                    });
//        });
        View btnCopy = view.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(v -> {
            final Context context = getContext();
            String content = inputView.getText().toString();
            if (context != null && !content.trim().isEmpty()) {
                ClipboardCompat.setText(context, "", content);
                ViewUtils.showToast(context, R.string.symja_prgm_message_copied, ViewUtils.LENGTH_SHORT);
            }
        });
        view.findViewById(R.id.btn_paste).setOnClickListener(v -> {
            final Context context = getContext();
            CharSequence clipboard = ClipboardCompat.getClipboard(context);
            if (clipboard != null) {
                inputView.insert(clipboard.toString());
            }
            inputView.requestFocus();
            if (context != null) {
                ViewUtils.showKeyboard(context, inputView);
            }
        });
        View btnUndo = view.findViewById(R.id.btn_undo);
        btnUndo.setOnClickListener(v -> {
            if (inputView.canUndo()) {
                inputView.undo();
            }
        });
        View btnRedo = view.findViewById(R.id.btn_redo);
        btnRedo.setOnClickListener(v -> {
            if (inputView.canRedo()) {
                inputView.redo();
            }
        });


        inputView.subscribeEvent(ContentChangeEvent.class, (event, unsubscribe) -> {
            btnRedo.setEnabled(inputView.canRedo());
            btnUndo.setEnabled(inputView.canUndo());
            btnCopy.setEnabled(inputView.getText().length() > 0);
        });
    }

    protected abstract ProgrammingResultAdapter getResultAdapter();

    protected void clickRun() {

    }

    @CallSuper
    protected void clickClearAll() {
        // reset position
    }

//   // TODO: replace with symja editor  private void changeTheme(EditorTheme editorTheme, View view) {
//        if (editorTheme != null && view != null) {
//            inputView.setTheme(editorTheme);
//            containerInput.setCardBackgroundColor(editorTheme.getBgColor());
//
//            // btnRun.setTextColor(editorTheme.getCaretColor());
//            // btnRun.setIconTint(ColorStateList.valueOf(editorTheme.getCaretColor()));
//            // ((TextView) view.findViewById(R.id.btn_clear)).setTextColor(editorTheme.getFgColor());
//            // ((TextView) view.findViewById(R.id.btn_paste)).setTextColor(editorTheme.getFgColor());
//            // ((TextView) view.findViewById(R.id.btn_copy)).setTextColor(editorTheme.getFgColor());
//
//            View divider = view.findViewById(R.id.divider);
//            //noinspection deprecation
//            divider.setBackgroundDrawable(new ColorDrawable(editorTheme.getGutterStyle().getFoldColor()));
//        }
//    }

    private void setupSymbolViews(final LinearLayout containerSymbol, final View rootView) {
        if (containerSymbol == null) {
            return;
        }
        CalculatorSettings settings = CalculatorSettings.newInstance(getContext());
        if (!settings.isShowSymbolBar()) {
            containerSymbol.setVisibility(View.GONE);
            return;
        }

        final ArrayList<HashMap<DragDirection, String>> directionTextMaps = new ArrayList<>();
        // Basic arithmetic operators
        directionTextMaps.add(DragButtonUtils.create("+", "-", "*", "/", "^"));
        // Parentheses and brackets
        directionTextMaps.add(DragButtonUtils.create(",", "(", ")", "{", "}"));
        // Inc, dec operators
        directionTextMaps.add(DragButtonUtils.create("<->", "=", ":=", "->", ":>"));
        // Shorthand operators
        directionTextMaps.add(DragButtonUtils.create("_", ">", ">=", "<", "<="));
        // Logic
        directionTextMaps.add(DragButtonUtils.create("!", "&&", "||", "/.", "/;"));
        // Relation
        directionTextMaps.add(DragButtonUtils.create("#", "==", "!=", "&", "%"));
        // numbers
        directionTextMaps.add(DragButtonUtils.create("0", "1", "2", "3", "4"));
        directionTextMaps.add(DragButtonUtils.create("5", "6", "7", "8", "9"));

        containerDragHint = rootView.findViewById(R.id.container_drag_hint);
        dragButtonPreview = rootView.findViewById(R.id.big_drag_button);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = rootView.getWidth();
                int desiredButtonWidth = ViewUtils.dpToPx(rootView.getContext(), 75);
                int count = width / desiredButtonWidth;
                containerSymbol.removeAllViews();
                for (int i = 0; i < Math.min(count, directionTextMaps.size()); i++) {
                    HashMap<DragDirection, String> directionTextMap = directionTextMaps.get(i);
                    @SuppressLint("InflateParams")
                    DragButton button = (DragButton) LayoutInflater.from(getContext()).inflate(R.layout.direction_button, null);
                    button.setDirectionTextMap(directionTextMap);
                    button.setOnDragListener(BaseProgrammingFragment.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.weight = 1;
                    containerSymbol.addView(button, params);
                }
                containerSymbol.setWeightSum(count);
            }
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            rootView.getWindowVisibleDisplayFrame(r);

            int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
            if (heightDiff > 300) { // if more than 100 pixels, its probably a keyboard...
                containerSymbol.setVisibility(View.VISIBLE);
            } else {
                containerSymbol.setVisibility(View.GONE);
                if (containerDragHint != null) {
                    containerDragHint.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean onDrag(@NonNull DragButton view, @NonNull DragDirection direction, String text, DragButton.State state) {
        if (state == DragButton.State.STOPPED) {
            if (containerDragHint != null) {
                containerDragHint.setVisibility(View.GONE);
            }
            if (getActivity() != null) {
                View currentFocus = getActivity().getWindow().getCurrentFocus();
                if (currentFocus instanceof CodeEditor) {
                    ((CodeEditor) currentFocus).insert(text);
                }
            }

        } else if (state == DragButton.State.DRAGGING) {
            if (dragButtonPreview != null) {
                dragButtonPreview.setSelectedDirection(direction);
            }
        } else if (state == DragButton.State.STARTED) {
            if (containerDragHint != null) {
                containerDragHint.setVisibility(View.VISIBLE);
            }
            if (dragButtonPreview != null) {
                dragButtonPreview.setDirectionTextMap(view.getDirectionTextMap());
            }
        }
        return false;
    }

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void clickOpenDocument(SuggestionItem item) {
        if (item.getAssetPath() != null) {
            MarkdownDocumentActivity.open(
                    this, new DocumentItem(item.getAssetPath(), item.getName(), item.getDescription()));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_OPEN_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            Context context = getContext();
            if (uri != null && inputView != null && context != null) {
                try {
                    // TODO: read in background
                    ContentResolver contentResolver = context.getContentResolver();
                    InputStream in = contentResolver.openInputStream(uri);
                    if (in == null) {
                        return;
                    }
                    if (in.available() > 0) {
                        int available = in.available();
                        int oneKilobytes = 1024;
                        int oneMegabytes = 1024 * oneKilobytes;
                        if (available > oneMegabytes) {
                            Toast.makeText(context, context.getString(R.string.symja_prgm_message_file_too_large), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    String content = IOUtils.toString(in, StandardCharsets.UTF_8);
                    in.close();
                    inputView.setText(content);
                } catch (Exception e) {
                    DLog.e(e);
                    Toast.makeText(context, context.getString(R.string.symja_prgm_message_cannot_import_file), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importTextFile();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = CalculatorSettings.newInstance(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //noinspection ConstantConditions
        @NonNull Context context = getActivity() != null ? getActivity() : getContext();

        inputView = view.findViewById(R.id.edit_input);
        inputView.setTextSize(15);
        inputView.replaceComponent(EditorAutoCompletion.class, new SymjaEditorAutoCompletion(inputView));

        listResultView = view.findViewById(R.id.calculation_result_recycler_view);
        listResultView.setLayoutManager(new LinearLayoutManager(context));

        ProgrammingResultAdapter adapter = getResultAdapter();
        adapter.setProgrammingItemClickListener(this);
        listResultView.setAdapter(adapter);
        listResultView.setHasFixedSize(true);

        btnRun = view.findViewById(R.id.btn_run);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        setupViews(view);
        addViewEvents(view);
        setupSymbolViews(view.findViewById(R.id.container_symbol), view);

        ImageView resizeInputButton = view.findViewById(R.id.btn_resize_input);
        resizeInputButton.setOnClickListener(v -> {
            inputExpanded.postValue(Boolean.FALSE.equals(inputExpanded.getValue()));
        });

        inputExpanded.observe(this.getViewLifecycleOwner(), expanded -> {
            ViewGroup.LayoutParams layoutParams = containerInput.getLayoutParams();
            if (expanded) {
                layoutParams.height = requireContext().getResources().getDimensionPixelSize(R.dimen.symja_prgm_input_height_large);
            } else {
                layoutParams.height = requireContext().getResources().getDimensionPixelSize(R.dimen.symja_prgm_input_height_small);
            }
            containerInput.requestLayout();
            if (expanded) {
                resizeInputButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.round_expand_less_24));
            } else {
                resizeInputButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.round_expand_more_24));
            }

            // Disable auto complete in collapsed mode
            inputView.getComponent(EditorAutoCompletion.class).setEnabled(expanded);
        });
        inputExpanded.postValue(true);

        view.findViewById(R.id.btn_open_menu).setOnClickListener(this::openMenu);
    }

    private void openMenu(View v) {
        if (getActivity() == null) {
            return;
        }
        AutoCloseablePopupMenu popupMenu = new AutoCloseablePopupMenu(requireActivity());
        popupMenu.inflate(R.menu.symja_prgm_menu_programming_console, v);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_clear_all_items) {
                clickClearAll();
                return true;
            }
            if (itemId == R.id.action_clear_input) {
                inputView.setText("");
                return true;
            } else if (itemId == R.id.action_import_text_file) {
                importTextFile();
                return true;
            }
            return false;
        });
        popupMenu.showAsDropDown(v);
    }


    private void importTextFile() {
        if (getContext() == null) {
            return;
        }
        AppAnalytics.getInstance(getContext()).logEvent(AppAnalyticsEvents.PROGRAMMING_IMPORT_TEXT_FILE, new Bundle());

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, RC_OPEN_FILE);
    }

    @Override
    public void onRemoveClicked(RecyclerView.ViewHolder holder) {

    }

    @Override
    public void onInputViewClicked(View view, @NonNull CalculationItem item) {
        inputView.setText(item.getInput().getValue());
        inputView.requestFocus();
        inputView.setSelection(inputView.getText().length());
    }

    @Override
    public void openWebView(@NonNull String html, @NonNull String baseUrl, @NonNull String mimeType) {
        InternalWebViewActivity.open(this, html, baseUrl, mimeType);
    }
}
