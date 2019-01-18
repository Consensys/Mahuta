package net.consensys.mahuta.core.exception;

public class ConnectionException extends TechnicalException {

    private static final long serialVersionUID = 8191498058841215578L;

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionException(Throwable cause) {
        super(cause);
    }
}
