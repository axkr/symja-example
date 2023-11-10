/*
 * Copyright (C) 2018 Duy Tran Le
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.symja.programming.autocomplete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.symja.programming.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by Duy on 23-May-17.
 */

public class FunctionSuggestionAdapter extends ArrayAdapter<SuggestionItem> {
    private final LayoutInflater inflater;
    private final List<SuggestionItem> displayItems;
    private final ArrayList<SuggestionItem> filterItems = new ArrayList<>();
    private final ArrayList<SuggestionItem> originalItems;
    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filterItems.clear();
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                for (SuggestionItem item : originalItems) {
                    if (item.getName().toLowerCase(Locale.US)
                            .startsWith(constraint.toString().toLowerCase(Locale.US))) {
                        filterItems.add(item);
                    }
                }
                filterResults.count = filterItems.size();
                filterResults.values = filterItems;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            ArrayList<SuggestionItem> filteredList = (ArrayList<SuggestionItem>) results.values;
            clear();
            if (filteredList != null) {
                addAll(filteredList);
            }
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            SuggestionItem documentItem = (SuggestionItem) resultValue;
            return super.convertResultToString(documentItem.getName());
        }
    };
    @Nullable
    private OnSuggestionClickListener onSuggestionListener;
    private OnSuggestionClickListener onSuggestionClickListener;

    public FunctionSuggestionAdapter(@NonNull Context context, @LayoutRes int resource, List<SuggestionItem> displayItems) {
        super(context, resource, displayItems);
        this.displayItems = displayItems;
        this.originalItems = new ArrayList<>(displayItems);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return displayItems.size();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.symja_prgm_list_item_suggest, parent, false);
        }
        final SuggestionItem documentItem = displayItems.get(position);

        TextView txtName = convertView.findViewById(R.id.txt_name);
        txtName.setText(documentItem.getName());

        TextView txtDescription = convertView.findViewById(R.id.txt_description);
        if (documentItem.getDescription() == null || documentItem.getDescription().isEmpty()) {
            txtDescription.setVisibility(View.GONE);
        } else {
            txtDescription.setVisibility(View.VISIBLE);
        }
        txtDescription.setText(documentItem.getDescription());

        View btnInfo = convertView.findViewById(R.id.img_info);
        btnInfo.setOnClickListener(v -> {
            if (onSuggestionListener != null) {
                onSuggestionListener.clickOpenDocument(documentItem);
            }
        });
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }


    public void setOnSuggestionListener(@Nullable OnSuggestionClickListener onSuggestionListener) {
        this.onSuggestionListener = onSuggestionListener;
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener onSuggestionClickListener) {
        this.onSuggestionClickListener = onSuggestionClickListener;
    }

    public interface OnSuggestionClickListener {
        void clickOpenDocument(SuggestionItem documentItem);
    }
}
