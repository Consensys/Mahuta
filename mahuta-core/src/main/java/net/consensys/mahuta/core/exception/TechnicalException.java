package net.consensys.mahuta.core.exception;

public class TechnicalException extends RuntimeException {

    private static final long serialVersionUID = 9201898433665734132L;

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TechnicalException(Throwable cause) {
        super(cause);
    }

}
