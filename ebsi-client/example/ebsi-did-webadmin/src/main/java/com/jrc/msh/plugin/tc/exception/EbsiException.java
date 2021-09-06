package com.jrc.msh.plugin.tc.exception;

public class EbsiException extends Exception {
    public EbsiException() {
    }

    public EbsiException(String message) {
        super(message);
    }

    public EbsiException(String message, Throwable cause) {
        super(message, cause);
    }

    public EbsiException(Throwable cause) {
        super(cause);
    }

    public EbsiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
