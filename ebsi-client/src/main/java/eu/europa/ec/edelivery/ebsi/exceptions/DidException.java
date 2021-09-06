package eu.europa.ec.edelivery.ebsi.exceptions;

public class DidException extends Exception{
    public DidException(String message) {
        super(message);
    }

    public DidException(String message, Throwable cause) {
        super(message, cause);
    }

    public DidException(Throwable cause) {
        super(cause);
    }

    protected DidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
