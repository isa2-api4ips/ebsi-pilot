package eu.europa.ec.edelivery.ebsi.exceptions;

public class RemoteCallException extends Exception {
    public RemoteCallException(String message) {
        super(message);
    }

    public RemoteCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteCallException(Throwable cause) {
        super(cause);
    }

    protected RemoteCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
