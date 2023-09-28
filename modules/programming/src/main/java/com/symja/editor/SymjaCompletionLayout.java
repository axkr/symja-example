package com.symja.editor;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.component.DefaultCompletionLayout;

public class SymjaCompletionLayout extends DefaultCompletionLayout {

    @NonNull
    @Override
    public View inflate(@NonNull Context context) {
        View view = super.inflate(context);

        ListView listView = this.getCompletionList();
        listView.setItemsCanFocus(true);
        return view;
    }
}
