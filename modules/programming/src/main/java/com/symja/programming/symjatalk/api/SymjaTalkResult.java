package com.symja.programming.symjatalk.api;


import com.symja.programming.console.models.CalculationItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SymjaTalkResult {
    private boolean success;
    private boolean error;
    private String version;
    private ArrayList<CalculationItem> calculationItems = new ArrayList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ArrayList<CalculationItem> getCalculationItems() {
        return calculationItems;
    }


    public void addResult(CalculationItem item) {
        calculationItems.add(item);
    }

    @NotNull
    @Override
    public String toString() {
        return calculationItems.toString();
    }
}
