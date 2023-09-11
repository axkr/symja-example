package com.symja.programming.document.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;


import com.duy.ide.editor.theme.model.EditorTheme;
import com.duy.ide.editor.view.CodeEditor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.symja.common.android.ClipboardCompat;
import com.symja.common.logging.DLog;
import com.symja.programming.R;
import com.symja.programming.document.view.ext.TeX;
import com.symja.programming.document.view.span.RoundedCornerSpan;
import com.symja.programming.utils.ViewUtils;
import com.symja.programming.view.NativeLatexView;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;

import java.util.HashMap;
import java.util.Stack;

@SuppressWarnings("unused")
public class MarkdownVisitor extends AbstractVisitor {
    private static final String TAG = "MarkdownVisitor";
    @NonNull
    private NativeMarkdownView container;
    @Nullable
    private MarkdownViewDelegate delegate;
    private Stack<SpannableStringBuilder> stack = new Stack<>();
    private HashMap<String, String> urlMap = new HashMap<>();

    @Nullable
    private NativeMarkdownTableView tableView;
    @Nullable
    private TableRow tableRow;
    private boolean inHeader = false;

    MarkdownVisitor(@NonNull NativeMarkdownView container, @Nullable MarkdownViewDelegate delegate) {
        this.container = container;
        this.delegate = delegate;
    }

    public MarkdownVisitor(@NonNull NativeMarkdownView container) {
        this.container = container;
    }

    public void setDelegate(@Nullable MarkdownViewDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: blockQuote = [" + blockQuote + "]");
        }


        // Begin visiting
        stack.push(new SpannableStringBuilder());

        visitChildren(blockQuote);

        // Stop visiting
        SpannableStringBuilder text = stack.pop();
        if (text.length() != 0) {

            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_markdown_quote,
                    container, false);
            container.addView(view);
            TextView textView = view.findViewById(R.id.text_view);
            setClickableIfNeeded(textView, text);
            textView.setText(text);
        }
    }

    @Override
    public void visit(BulletList bulletList) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: bulletList = [" + bulletList + "]");
        }
        visitChildren(bulletList);
    }

    /**
     * Inline code
     */
    @Override
    public void visit(Code code) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: code = [" + code + "] next = " + code.getNext()
                    + "; parent = " + code.getParent());
        }
        SpannableString span = new SpannableString(code.getLiteral());
        int textColor = ViewUtils.getColor(getContext(), android.R.attr.textColorPrimary);
        span.setSpan(new TypefaceSpan("monospace"),
                0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int backgroundColor = ViewUtils.getColor(getContext(), android.R.attr.textColorPrimary);
        backgroundColor = Color.argb(20, Color.red(backgroundColor),
                Color.green(backgroundColor), Color.blue(backgroundColor));
        span.setSpan(new RoundedCornerSpan(backgroundColor, textColor, ViewUtils.dpToPx(getContext(), 3)),
                0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!stack.isEmpty()) {
            SpannableStringBuilder builder = stack.peek();
            builder.append(span);
        } else {
            makeBodyTextView(span);
        }
    }

    /**
     * Root of document
     */
    @Override
    public void visit(Document document) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: document = [" + document + "]");
        }
        visitChildren(document);
    }

    @Override
    public void visit(Emphasis emphasis) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: emphasis = [" + emphasis + "]");
        }
        visitEmphasis(emphasis, emphasis.getOpeningDelimiter(), emphasis.getClosingDelimiter());
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: fencedCodeBlock = [" + fencedCodeBlock + "]");
        }
        super.visit(fencedCodeBlock);
        String literal = fencedCodeBlock.getLiteral();
        if (DLog.DEBUG) {
            DLog.d(TAG, "literal = " + literal);
        }

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.list_item_markdown_codeblock, container, false);
        container.addView(view);
        CodeEditor codeEditor = view.findViewById(R.id.code_editor);
        final String code = fencedCodeBlock.getLiteral().trim();
        codeEditor.setText(code);
        codeEditor.setCursorVisible(false);
        codeEditor.setEnabled(false);
        codeEditor.getDocument().setMode("symja");

        EditorTheme editorTheme = codeEditor.getEditorTheme();
        if (editorTheme != null) {
            CardView cardView = (CardView) view;
            cardView.setCardBackgroundColor(editorTheme.getBgColor());
            View divider = view.findViewById(R.id.divider);
            MaterialButton btnCopy = view.findViewById(R.id.btn_copy);
            if (code.contains(">> ")) {
                btnCopy.setVisibility(View.VISIBLE);
                btnCopy.setTextColor(editorTheme.getFgColor());
                btnCopy.setIconTint(ColorStateList.valueOf(editorTheme.getFgColor()));
                divider.setVisibility(View.VISIBLE);
                divider.setBackgroundDrawable(new ColorDrawable(editorTheme.getGutterStyle().getFoldColor()));

                btnCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (delegate != null) {
                            delegate.onCopyCodeButtonClicked(v, code);
                        }
                    }
                });

            } else {
                btnCopy.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: hardLineBreak = [" + hardLineBreak + "]");
        }
        super.visit(hardLineBreak);
    }

    @Override
    public void visit(Heading heading) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: heading = [" + heading + "]");
        }

        // Begin visiting
        stack.push(new SpannableStringBuilder());

        visitChildren(heading);

        // Stop visiting
        SpannableStringBuilder textContent = stack.pop();

        MaterialTextView textView = new MaterialTextView(container.getContext());

        int level = heading.getLevel();
        switch (level) {
            case 1:
                textView.setTextAppearance(container.getContext(), com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline4);
                break;
            case 2:
                textView.setTextAppearance(container.getContext(), com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline5);
                break;
            case 3:
            default:
                textView.setTextAppearance(container.getContext(),com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6);
                break;
        }
        textView.setPadding(0, ViewUtils.dpToPx(getContext(), 8), 0, ViewUtils.dpToPx(getContext(), 8));
        textView.setText(textContent);
        textView.setTag("#" + textContent.toString().toLowerCase().replaceAll("\\W", ""));
        setClickableIfNeeded(textView, textContent);
        container.addView(textView);
    }

    @Override

    public void visit(ThematicBreak thematicBreak) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: thematicBreak = [" + thematicBreak + "]");
        }
        super.visit(thematicBreak);
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: htmlInline = [" + htmlInline + "]");
        }
        super.visit(htmlInline);

    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: htmlBlock = [" + htmlBlock + "]");
        }
    }

    @Override
    public void visit(Image image) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: image = [" + image + "]");
        }
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: indentedCodeBlock = [" + indentedCodeBlock + "]");
        }
    }

    @Override
    public void visit(Link link) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: link = [" + link + "]");
        }
        stack.push(new SpannableStringBuilder());
        visitChildren(link); // Text
        final SpannableStringBuilder text = stack.pop();

        final String url = link.getDestination();
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                System.out.println("MarkdownVisitor.onClick");
                if (delegate != null) {
                    delegate.onLinkClick(text, url);
                }
            }
        }, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        urlMap.put(text.toString(), url);

        if (!stack.isEmpty()) {
            stack.peek().append(text);
        } else {
            makeBodyTextView(text);
        }
    }

    @Override
    public void visit(ListItem listItem) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: listItem = [" + listItem + "]");
        }

        // Begin visiting
        stack.push(new SpannableStringBuilder());
        visitChildren(listItem);

        // Stop visiting
        final SpannableStringBuilder text = stack.pop();

        View container = LayoutInflater.from(getContext())
                .inflate(R.layout.list_item_markdown_item, this.container, false);
        this.container.addView(container);

        TextView textView = container.findViewById(R.id.text_view);

        // if the content of list item is a link, long click to copy.
        ClickableSpan[] spans = text.getSpans(0, 0, ClickableSpan.class);
        if (spans != null && spans.length == 1
                && text.getSpanStart(spans[0]) == 0
                && text.getSpanEnd(spans[0]) == text.length()) {
            final ClickableSpan clickableSpan = spans[0];
            final String url = urlMap.get(text.toString());
            if (url != null) {
                text.removeSpan(clickableSpan);
                text.setSpan(new ForegroundColorSpan(ViewUtils.getAccentColor(getContext())),
                        0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (delegate != null) {
                            delegate.onLinkClick(text, url);
                        }
                    }
                });
                container.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardCompat.setText(v.getContext(), text.toString(), url);
                        ViewUtils.showToast(v.getContext(), R.string.symja_prgm_message_copied, ViewUtils.LENGTH_SHORT);
                        return true;
                    }
                });
                container.setClickable(true);
                container.setFocusable(true);

                textView.setTextIsSelectable(false);

                // debugging
                textView.setTag("link");
            }
            textView.setText(text);
        } else {
            textView.setText(text);
            setClickableIfNeeded(textView, text);
        }
    }

    @Override
    public void visit(OrderedList orderedList) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: orderedList = [" + orderedList + "]");
        }
        visitList(orderedList);
    }

    @Override
    public void visit(Paragraph paragraph) {
        long l = System.currentTimeMillis();
        if (DLog.DEBUG) {
            DLog.d(TAG, "begin visit paragraph = [" + paragraph + "] " + l);
        }

        boolean newView = false;
        if (paragraph.getParent() == null || paragraph.getParent() instanceof Document || stack.isEmpty()) {
            stack.push(new SpannableStringBuilder());
            newView = true;
        }

        visitChildren(paragraph);

        if (newView) {
            // Stop visiting
            makeBodyTextView(stack.pop());
        }

        if (DLog.DEBUG) {
            DLog.d(TAG, "end visit paragraph = [" + paragraph + "] " + l);
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: softLineBreak = [" + softLineBreak + "]");
        }
        if (!stack.isEmpty()) {
            stack.peek().append(' ');
        }
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: strongEmphasis = [" + strongEmphasis + "]");
        }
        visitEmphasis(strongEmphasis, strongEmphasis.getOpeningDelimiter(), strongEmphasis.getClosingDelimiter());
    }

    @Override
    public void visit(Text text) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: text = [" + text + "]" +
                    " next = " + text.getNext() + "; parent = " + text.getParent());
        }
        if (!stack.isEmpty()) {
            stack.peek().append(text.getLiteral());
        } else {
            stack.push(new SpannableStringBuilder(text.getLiteral()));
            visitChildren(text);
            SpannableStringBuilder stringBuilder = stack.pop();
            makeBodyTextView(stringBuilder);
        }
    }

    @Override
    public void visit(CustomBlock customBlock) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: customBlock = [" + customBlock + "]" +
                    " next = " + customBlock.getNext() + "; parent = " + customBlock.getParent());
        }
        if (customBlock instanceof TableBlock) {
            TableBlock tableBlock = (TableBlock) customBlock;
            visitTableBlock(tableBlock);
        } else {
            visitChildren(customBlock);
        }
    }

    @Override
    public void visit(CustomNode customNode) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "visit() called with: customNode = [" + customNode + "]" +
                    " next = " + customNode.getNext() + "; parent = " + customNode.getParent());
        }
        if (customNode instanceof TableHead) {
            inHeader = true;
            visitChildren(customNode);

        } else if (customNode instanceof TableBody) {

            inHeader = false;
            visitChildren(customNode);

        } else if (customNode instanceof org.commonmark.ext.gfm.tables.TableRow) {
            if (tableView == null) {
                throw new RuntimeException("Parsing a table row without a table view");
            }
            tableRow = new TableRow(getContext());
            tableView.addView(tableRow);
            visitChildren(customNode);
            tableRow = null;

        } else if (customNode instanceof TableCell) {
            if (tableRow == null) {
                throw new RuntimeException("Parsing a table cell without a table row");
            }
            TableCell cell = (TableCell) customNode;
            NativeMarkdownView parentView = this.container;

            // enter table cell
            NativeMarkdownView cellView = new NativeMarkdownView(getContext());
            this.container = cellView;
            tableRow.addView(cellView);
            int fourDpInPx = ViewUtils.dpToPx(getContext(), 4);
            cellView.setPadding(fourDpInPx, fourDpInPx, fourDpInPx, fourDpInPx);

            stack.push(new SpannableStringBuilder());
            visitChildren(cell);
            SpannableStringBuilder text = stack.pop();
            if (inHeader) {
                text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            makeBodyTextView(text);

            // exit table cell
            this.container = parentView;
        } else if (customNode instanceof TeX) {
            visitTeXNode((TeX) customNode);
        } else {
            visitChildren(customNode);
        }

    }

    private void visitTeXNode(TeX teXNode) {
        SpannableStringBuilder content = new SpannableStringBuilder();
        stack.push(content);
        visitChildren(teXNode);
        stack.pop();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_markdown_mathview, container, false);
        this.container.addView(view);

        NativeLatexView mathJaxView = view.findViewById(R.id.output_view);
        mathJaxView.setText(content.toString());

        System.out.println("content = " + content);
    }

    private void visitTableBlock(TableBlock tableBlock) {
        try {
            HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
            scrollView.setFillViewport(true);
            scrollView.setScrollBarFadeDuration(Integer.MAX_VALUE);
            scrollView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);

            tableView = new NativeMarkdownTableView(getContext());
            tableView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

            container.addView(scrollView);
            scrollView.addView(tableView);

            visitChildren(tableBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void visitEmphasis(Delimited emphasis, String openingDelimiter, String closingDelimiter) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "openingDelimiter = " + openingDelimiter);
        }

        stack.push(new SpannableStringBuilder());
        visitChildren((Node) emphasis);

        SpannableStringBuilder text = stack.pop();
        switch (openingDelimiter) {
            case "__":
            case "**":
                text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "*":
            case "_":
                text.setSpan(new StyleSpan(Typeface.ITALIC), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "~~":
                text.setSpan(new StrikethroughSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
        }

        if (!stack.isEmpty()) {
            stack.peek().append(text);
        } else {
            makeBodyTextView(text);
        }
    }

    private void makeBodyTextView(CharSequence text) {
        MaterialTextView textView = new MaterialTextView(container.getContext());
        textView.setTextAppearance(getContext(), com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
        textView.setText(text);
        if (text instanceof Spanned) {
            setClickableIfNeeded(textView, (Spanned) text);
        }
        container.addView(textView);
    }

    private void setClickableIfNeeded(TextView textView, Spanned text) {
        ClickableSpan[] spans = text.getSpans(0, text.length(), ClickableSpan.class);
        if (spans != null && spans.length > 0) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setClickable(true);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
        } else {
            textView.setTextIsSelectable(true);
        }
    }

    private Context getContext() {
        return container.getContext();
    }

    private void visitList(Node node) {
        visitChildren(node);
    }

}
