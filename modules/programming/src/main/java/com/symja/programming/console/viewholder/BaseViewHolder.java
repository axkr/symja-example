package com.symja.programming.console.viewholder;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.symja.common.android.ClipboardCompat;
import com.symja.common.datastrcture.Data;
import com.symja.programming.R;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.utils.ViewUtils;
import com.symja.programming.view.popupmenu.AutoCloseablePopupMenu;
import com.symja.programming.view.popupmenu.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    private final TextView txtTitle;

    @Nullable
    private final TextView txtErrorMessage;
    @Nullable
    private final TextView txtStandardMessage;
    @Nullable
    private final View containerMessage;
    @Nullable
    private final TextView expandCollapseButton;

    private final View btnMenu;

    BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        txtTitle = itemView.findViewById(R.id.txt_title);

        txtErrorMessage = itemView.findViewById(R.id.txt_error_message);
        txtStandardMessage = itemView.findViewById(R.id.txt_standard_message);
        containerMessage = itemView.findViewById(R.id.container_output_message);
        expandCollapseButton = itemView.findViewById(R.id.expand_collapse_button);

        btnMenu = itemView.findViewById(R.id.btn_more);
    }

    @CallSuper
    public void bindData(@NonNull final CalculationItem item,
                         final OnProgrammingItemClickListener onProgrammingItemClickListener) {

        final SpannableStringBuilder titleText = new SpannableStringBuilder();
        if (item.getTitle() != null && !item.getTitle().isEmpty()) {
            titleText.append(item.getTitle());

        } else {
            // titleText.append(calculationItem.getInput().getValue());
            // String missingBracket = Scanner.balanceCode(calculationItem.getInput().getValue());
            // if (missingBracket != null && missingBracket.length() > 0) {
            //     SpannableString text = new SpannableString(missingBracket);
            //     text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(itemView.getContext(), R.attr.colorError)),
            //             0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //     titleText.append(text);
            // }
            titleText.append(itemView.getResources().getString(R.string.symja_prgm_label_calculation_result));
        }
        txtTitle.setText(titleText);
        txtTitle.setOnClickListener(v -> onProgrammingItemClickListener.onInputViewClicked(v, item));
        Context context = txtTitle.getContext();

        if ((item.getStdOut() != null && !item.getStdOut().isEmpty())
                || (item.getStdErr() != null && !item.getStdErr().isEmpty())) {
            if (this.containerMessage != null) {
                this.containerMessage.setVisibility(View.VISIBLE);
            }

            if (item.getStdErr() != null && !item.getStdErr().isEmpty()) {
                if (txtErrorMessage != null) {
                    txtErrorMessage.setVisibility(View.VISIBLE);
                    txtErrorMessage.setText(item.getStdErr());
                }
            } else {
                if (txtErrorMessage != null) {
                    txtErrorMessage.setVisibility(View.GONE);
                }
            }
            if (item.getStdOut() != null && !item.getStdOut().isEmpty()) {
                if (txtStandardMessage != null) {
                    txtStandardMessage.setVisibility(View.VISIBLE);
                    txtStandardMessage.setText(item.getStdOut());
                }
            } else {
                if (txtStandardMessage != null) {
                    txtStandardMessage.setVisibility(View.GONE);
                }
            }

            String expanded = "expanded";
            if (expandCollapseButton != null) {
                this.expandCollapseButton.setTag(expanded);
                this.expandCollapseButton.setOnClickListener(v -> {
                    if (this.expandCollapseButton.getTag().equals(expanded)) {
                        this.expandCollapseButton.setTag("collapsed");
                        if (txtStandardMessage != null) {
                            txtStandardMessage.setVisibility(View.GONE);
                        }
                        if (txtErrorMessage != null) {
                            txtErrorMessage.setVisibility(View.GONE);
                        }
                    } else {
                        this.expandCollapseButton.setTag(expanded);
                        if (txtStandardMessage != null) {
                            txtStandardMessage.setVisibility(View.VISIBLE);
                        }
                        if (txtErrorMessage != null) {
                            txtErrorMessage.setVisibility(View.VISIBLE);
                        }
                    }
                    if (expandCollapseButton.getTag().equals(expanded)) {
                        expandCollapseButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.symja_prgm_baseline_keyboard_arrow_down_24),
                                null, null, null
                        );
                    } else {
                        expandCollapseButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.symja_prgm_baseline_keyboard_arrow_right_24),
                                null, null, null
                        );
                    }
                });
            }
        } else {
            if (this.containerMessage != null) {
                this.containerMessage.setVisibility(View.GONE);
            }
        }


        // Popup menu
        boolean showMenuButton = !item.getInput().isEmpty();
        if (!item.getData().isEmpty()) {
            showMenuButton = true;
        }
        if (showMenuButton) {
            btnMenu.setVisibility(View.VISIBLE);
            btnMenu.setOnClickListener(v -> showContextMenu(v, item, onProgrammingItemClickListener));
        } else {
            btnMenu.setVisibility(View.GONE);
        }

    }

    private void showContextMenu(@NotNull View anchorView, @NotNull CalculationItem calculationItem, OnProgrammingItemClickListener onProgrammingItemClickListener) {
        Context context = anchorView.getContext();
        AutoCloseablePopupMenu popupMenu = new AutoCloseablePopupMenu((FragmentActivity) context);

        List<Pair<Integer, Data>> itemIds = new ArrayList<>();

        if (!calculationItem.getInput().isEmpty()) {
            int itemId = View.generateViewId();
            Data data = calculationItem.getInput();
            itemIds.add(new Pair<>(itemId, data));
            MenuItem item = new MenuItem(itemId,
                    context.getString(R.string.symja_prgm_menu_copy_input_as, data.getFormat().getName()),
                    data.getValue());
            item.setIcon(R.drawable.symja_prgm_baseline_content_copy_24);
            popupMenu.addItem(item);
        }

        for (Data data : calculationItem.getDataList()) {
            int itemId = View.generateViewId();
            itemIds.add(new Pair<>(itemId, data));
            MenuItem item = new MenuItem(itemId, context.getString(R.string.symja_prgm_menu_copy_result_as, data.getFormat().getName()), data.getValue());
            item.setIcon(R.drawable.symja_prgm_baseline_content_copy_24);
            popupMenu.addItem(item);
        }

        MenuItem removeItem = new MenuItem(R.id.symja_prgm_action_remove, context.getString(R.string.symja_prgm_button_remove));
        removeItem.setIcon(R.drawable.symja_prgm_baseline_clear_24);
        popupMenu.addItem(removeItem);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.symja_prgm_action_remove) {
                if (onProgrammingItemClickListener != null) {
                    onProgrammingItemClickListener.onRemoveClicked(BaseViewHolder.this);
                }
            } else {
                Pair<Integer, Data> pair = itemIds.stream().filter(x -> x.first.equals(itemId)).findFirst().orElse(null);
                if (pair != null) {
                    Data data = pair.second;
                    ClipboardCompat.setText(context, data.getFormat().getName(), data.getValue());
                    Toast.makeText(context, R.string.symja_prgm_message_copied, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            return false;
        });
        ViewUtils.hideKeyboard(context, anchorView);
        popupMenu.showAsDropDown(anchorView);
    }
}
