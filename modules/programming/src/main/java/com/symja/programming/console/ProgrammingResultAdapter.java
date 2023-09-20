package com.symja.programming.console;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.symja.common.datastrcture.Data;
import com.symja.programming.R;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.console.viewholder.BaseViewHolder;
import com.symja.programming.console.viewholder.FactorDiagramViewHolder;
import com.symja.programming.console.viewholder.LatexViewHolder;
import com.symja.programming.console.viewholder.MarkdownViewHolder;
import com.symja.programming.console.viewholder.SpaceViewHolder;
import com.symja.programming.console.viewholder.TextViewHolder;
import com.symja.programming.console.viewholder.WebViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ProgrammingResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnProgrammingItemClickListener {

    @NonNull
    private List<CalculationItem> document;
    @NonNull
    private final LayoutInflater inflater;
    private boolean useSpace = false;
    @Nullable
    private OnProgrammingItemClickListener programmingItemClickListener;

    public ProgrammingResultAdapter(@NonNull Context context, @NonNull List<CalculationItem> document) {
        this.inflater = LayoutInflater.from(context);
        this.document = document;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case ViewType.SPACE:
                return new SpaceViewHolder(inflater.inflate(R.layout.symja_prgm_programming_list_item_space, viewGroup, false));
            case ViewType.LATEX:
                return new LatexViewHolder(inflater.inflate(R.layout.symja_prgm_programming_list_item_result_latex, viewGroup, false));

            case ViewType.WEB:
                return new WebViewHolder(inflater.inflate(R.layout.symja_prgm_programming_list_item_result_web, viewGroup, false));

            case ViewType.TEXT:
                return new TextViewHolder(inflater.inflate(R.layout.symja_prgm_programming_list_item_result_text, viewGroup, false));
            case ViewType.FACTOR_DIAGRAM:
                return new FactorDiagramViewHolder(inflater.inflate(R.layout.symja_prgm_programming_list_item_result_factor_diagram, viewGroup, false));
            case ViewType.MARKDOWN:
                return new MarkdownViewHolder(inflater.inflate(R.layout.symja_prgm_programming_list_item_markdown, viewGroup, false));
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder h, final int position) {
        if (h instanceof SpaceViewHolder) {
            return;
        }

        final CalculationItem calculationItem = document.get(position - (useSpace ? 1 : 0));
        if (h instanceof BaseViewHolder) {
            ((BaseViewHolder) h).bindData(calculationItem, this);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // header
        if (position == 0 && useSpace) {
            return ViewType.SPACE;
        }
        if (useSpace) {
            position = position - 1;
        }

        CalculationItem calculationItem = document.get(position);

        if (calculationItem.hasFormat(Data.Format.LATEX)) {
            return ViewType.LATEX;
        }

        Data.Format type = calculationItem.getType();

        if (type == Data.Format.SVG) {
            return ViewType.SVG;
        } else if (type == Data.Format.HTML || type == Data.Format.IFRAME) {
            return ViewType.WEB;
        } else if (type == Data.Format.LATEX) {
            return ViewType.LATEX;
        } else if (type == Data.Format.FACTOR_DIAGRAM) {
            return ViewType.FACTOR_DIAGRAM;
        } else if (type == Data.Format.MARKDOWN) {
            return ViewType.MARKDOWN;
        }

        return ViewType.TEXT;
    }

    @Override
    public int getItemCount() {
        return document.size() + (useSpace ? 1 : 0);
    }

    public void setUseSpace(boolean useSpace) {
        this.useSpace = useSpace;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDocument(@NonNull ProgrammingConsoleDocument document) {
        this.document = document;
        notifyDataSetChanged();
    }

    @Override
    public void onRemoveClicked(@NotNull RecyclerView.ViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition >= 0) {
            int dataPosition = adapterPosition - (useSpace ? 1 : 0);
            document.remove(dataPosition);
            notifyItemRemoved(adapterPosition);
        }
    }

    @Override
    public void onInputViewClicked(View view, @NotNull CalculationItem item) {
        if (programmingItemClickListener != null) {
            programmingItemClickListener.onInputViewClicked(view, item);
        }
    }

    @Override
    public void openWebView(@NonNull String html, @NonNull String baseUrl, @NonNull String mimeType) {
        if (programmingItemClickListener != null) {
            programmingItemClickListener.openWebView(html, baseUrl, mimeType);
        }
    }

    public void setProgrammingItemClickListener(@Nullable OnProgrammingItemClickListener programmingItemClickListener) {
        this.programmingItemClickListener = programmingItemClickListener;
    }

    static class ViewType {
        static final int SPACE = 0;
        static final int LATEX = 3;
        static final int WEB = 4;
        static final int SVG = 5;
        static final int TEXT = 6;
        static final int FACTOR_DIAGRAM = 7;
        static final int MARKDOWN = 8;
    }


}
