package com.symja.programming.console.viewholder;

import android.view.View;

import androidx.annotation.NonNull;


import com.symja.programming.R;
import com.symja.programming.console.OnProgrammingItemClickListener;
import com.symja.programming.console.models.CalculationItem;
import com.symja.programming.view.FactorDiagramView;

public class FactorDiagramViewHolder extends BaseViewHolder {
    private final FactorDiagramView factorDiagramView;

    public FactorDiagramViewHolder(@NonNull View itemView) {
        super(itemView);
        factorDiagramView = itemView.findViewById(R.id.factor_diagram_view);
    }

    @Override
    public void bindData(@NonNull CalculationItem item, OnProgrammingItemClickListener onProgrammingItemClickListener) {
        super.bindData(item, onProgrammingItemClickListener);

        if (item.getData().isEmpty()) {
            factorDiagramView.setVisibility(View.GONE);

        } else {
            factorDiagramView.setVisibility(View.VISIBLE);
            try {
                int number = Integer.parseInt(item.getData());
                factorDiagramView.setNumber(number);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
