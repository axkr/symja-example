package com.symja.programming.document;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.symja.common.android.ClipboardCompat;
import com.symja.programming.R;

import java.util.ArrayList;
import java.util.List;

public class ExpressionCopyingDialog {

    public static void show(final Context context, final View anchorView, String code) {
        if (context == null || code == null) {
            return;
        }
        final List<CharSequence> inputs = new ArrayList<>();
        // parse input
        String[] lines = code.split("[\n\r]");
        for (String line : lines) {
            if (line.startsWith(">>")) {
                inputs.add(line.substring(2).trim());
            }
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.symja_prgm_title_menu_copy_code))
                .setItems(inputs.toArray(new CharSequence[0]),
                        (dialog, which) -> {
                            String input = inputs.get(which).toString();
                            ClipboardCompat.setText(context, "", input);
                            if (anchorView != null) {
                                Snackbar.make(anchorView, R.string.symja_prgm_message_copied, Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(context, R.string.symja_prgm_message_copied, Toast.LENGTH_SHORT).show();
                            }
                        })
                .create()
                .show();
    }
}
