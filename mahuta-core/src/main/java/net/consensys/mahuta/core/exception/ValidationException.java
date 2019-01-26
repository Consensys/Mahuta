package net.consensys.mahuta.core.exception;

public class ValidationException extends MahutaException {

    private static final long serialVersionUID = -1577067446504139812L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

}
