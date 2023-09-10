package com.symja.programming.document.view.ext;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

public class TeXExtension implements Parser.ParserExtension {

    private TeXExtension() {
    }

    public static Extension create() {
        return new TeXExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new TeXDelimiterProcessor());
    }

}
