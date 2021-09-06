package eu.europa.ec.edelivery.ebsi.exceptions;

public class TimestampException extends Exception{
    public TimestampException(String message) {
        super(message);
    }

    public TimestampException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimestampException(Throwable cause) {
        super(cause);
    }

    protected TimestampException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
