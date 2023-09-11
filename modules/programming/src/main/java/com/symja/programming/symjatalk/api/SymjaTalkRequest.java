package com.symja.programming.symjatalk.api;

import androidx.annotation.NonNull;

import com.google.common.collect.Lists;

import java.util.List;


public class SymjaTalkRequest {

    @NonNull
    private final String input;
    @NonNull
    private final List<SymjaFormat> outputFormats;

    public SymjaTalkRequest(@NonNull String input, @NonNull SymjaFormat format) {
        this(input, Lists.newArrayList(format));
    }

    public SymjaTalkRequest(@NonNull String input, @NonNull List<SymjaFormat> outputFormats) {
        this.input = input;
        this.outputFormats = outputFormats;
    }

    @NonNull
    public String getInput() {
        return input;
    }

    @NonNull
    public List<SymjaFormat> getOutputFormats() {
        return outputFormats;
    }

}
