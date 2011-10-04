package com.res.java.lib.exceptions;

@SuppressWarnings("serial")
public class InvalidDataFormatException extends RuntimeException {
    public InvalidDataFormatException(String message) {
        super(message);
    }
}

