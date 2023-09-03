package org.lhotse.config.core.exception;

public class LhotseException extends RuntimeException {
    public LhotseException() {
    }

    public LhotseException(String message) {
        super(message);
    }

    public LhotseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LhotseException(Throwable cause) {
        super(cause);
    }

    public LhotseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
