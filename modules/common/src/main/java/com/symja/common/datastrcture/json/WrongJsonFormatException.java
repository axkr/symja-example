package com.symja.common.datastrcture.json;

public class WrongJsonFormatException extends RuntimeException {
    public WrongJsonFormatException(JsonMap properties) {
        super("Couldn't find appropriate token with properties: " + properties);
    }
}
