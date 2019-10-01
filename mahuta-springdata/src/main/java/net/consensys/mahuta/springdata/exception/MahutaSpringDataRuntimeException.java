package net.consensys.mahuta.springdata.exception;

public class MahutaSpringDataRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -2583815459528545639L;

    public MahutaSpringDataRuntimeException(String m, Throwable e) {
        super(m, e);
    }
    
    public MahutaSpringDataRuntimeException(String m) {
        super(m);
    }
}
