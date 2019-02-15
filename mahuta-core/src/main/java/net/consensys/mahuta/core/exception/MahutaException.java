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
}