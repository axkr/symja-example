package com.symja.editor;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.symja.common.logging.DLog;
import com.symja.programming.R;

import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class SymjaCompletionAdapter extends EditorCompletionAdapter {
    private static final String TAG = "SymjaCompletionAdapter";

    @Nullable
    private OnItemClickListener onItemClickListener;

    @Override
    public int getItemHeight() {
        // 45 dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getContext().getResources().getDisplayMetrics());
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent, boolean isCurrentCursorPosition) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.symja_prgm_editor_completion_result_item, parent, false);
        }
        CompletionItem item = getItem(pos);

        TextView tv = view.findViewById(R.id.result_item_label);
        tv.setText(item.label);
        tv.setTextColor(getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_PRIMARY));

        tv = view.findViewById(R.id.result_item_desc);
        tv.setText(item.desc);
        tv.setTextColor(getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_SECONDARY));

        view.setTag(pos);
        if (isCurrentCursorPosition) {
            view.setBackgroundColor(getThemeColor(EditorColorScheme.COMPLETION_WND_ITEM_CURRENT));
        } else {
            view.setBackgroundColor(0);
        }
        ImageView iv = view.findViewById(R.id.result_item_image);
        iv.setImageDrawable(item.icon);
        iv.setClickable(true);
        iv.setFocusable(true);
        iv.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                try {
                    onItemClickListener.onIconClick(pos, item);
                } catch (Exception e) {
                    DLog.w(TAG, e);
                }
            }
        });
        view.setOnClickListener(v -> {
            try {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(pos, item);
                }
            } catch (Exception e) {
                DLog.w(TAG, e);
            }
        });
        return view;
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onIconClick(int position, CompletionItem completionItem);

        void onItemClick(int position, CompletionItem completionItem);
    }
}
