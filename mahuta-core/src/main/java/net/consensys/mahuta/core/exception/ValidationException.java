package net.consensys.mahuta.core.exception;

public class ValidationException extends MahutaException {

    private static final long serialVersionUID = -1577067446504139812L;

    public ValidationException(String messageFormat, Object... args) {
        this(String.format(messageFormat, args));
    }
    
    public ValidationException(String message) {
        super(message);
    }

}
