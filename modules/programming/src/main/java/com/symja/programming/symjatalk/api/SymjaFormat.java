package com.symja.programming.symjatalk.api;

public enum SymjaFormat {
    PLAIN_TEXT("plaintext", true, true),
    LATEX("latex", true, true),
    SYMJA("sinput", true, true),
    MARKDOWN("markdown", false, true),
    MATHML("mathml", false, true),
    JSXGRAPH("jsxgraph", false, true),
    MATHCELL("mathcell", false, true),
    PLOTLY("plotly", false, true),
    VISJS("visjs", false, true),
    HTML("html", false, true);

    private final String rawValue;
    private final boolean inputSupport;
    private final boolean outputSupport;

    SymjaFormat(String rawValue, boolean input, boolean outputSupport) {
        this.rawValue = rawValue;
        this.inputSupport = input;
        this.outputSupport = outputSupport;
    }

    public boolean isInputSupport() {
        return inputSupport;
    }

    public boolean isOutputSupport() {
        return outputSupport;
    }

    public String getRawValue() {
        return rawValue;
    }
}