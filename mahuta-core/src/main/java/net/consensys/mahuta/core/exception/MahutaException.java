package net.consensys.mahuta.core.exception;

/**
 * MahutaException
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public class MahutaException extends RuntimeException {

    private static final long serialVersionUID = 3153727787764151573L;

    public MahutaException(String message) {
        super(message);
    }

    public MahutaException(String message, Throwable cause) {
        super(message, cause);
    }

    public MahutaException(Throwable cause) {
        super(cause);
    }

}