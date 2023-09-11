package com.symja.programming.document;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.symja.programming.R;
import com.symja.programming.document.model.DocumentItem;

public class DocumentNavigationAdapter extends ListAdapter<DocumentItem, DocumentNavigationAdapter.ViewHolder> {

    private OnItemClick onItemClickListener;

    protected DocumentNavigationAdapter() {
        super(new DiffUtil.ItemCallback<DocumentItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull DocumentItem oldItem, @NonNull DocumentItem newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(@NonNull DocumentItem oldItem, @NonNull DocumentItem newItem) {
                return oldItem.equals(newItem);
            }
        });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.symja_prgm_document_navigation_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentItem item = getItem(position);
        holder.name.setText(item.getName());
        holder.name.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position, item);
            }
        });
    }

    public void setOnItemClickListener(OnItemClick onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClick {
        void onItemClick(int position, DocumentItem documentItem);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_name);
        }
    }
}
