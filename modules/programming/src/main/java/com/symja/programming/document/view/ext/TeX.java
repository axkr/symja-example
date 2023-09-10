package com.symja.programming.document.view.ext;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * A TeX node containing text and other inline nodes nodes as children.
 */
public class TeX extends CustomNode implements Delimited {

    private static final String DELIMITER = "$$";

    @Override
    public String getOpeningDelimiter() {
        return DELIMITER;
    }

    @Override
    public String getClosingDelimiter() {
        return DELIMITER;
    }
}
