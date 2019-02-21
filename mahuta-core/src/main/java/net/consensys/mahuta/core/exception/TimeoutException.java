package net.consensys.mahuta.core.exception;

public class TimeoutException extends MahutaException {

    private static final long serialVersionUID = -1577067446504139812L;

    public TimeoutException(String messageFormat, Object... args) {
        this(String.format(messageFormat, args));
    }
    
    public TimeoutException(String message) {
        super(message);
    }

}
