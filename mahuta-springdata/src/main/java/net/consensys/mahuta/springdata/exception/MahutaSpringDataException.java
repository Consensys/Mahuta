package net.consensys.mahuta.springdata.exception;

public class MahutaSpringDataException extends RuntimeException {

    private static final long serialVersionUID = -2583815459528545639L;

    public MahutaSpringDataException(String m, Throwable e) {
        super(m, e);
    }
    
    public MahutaSpringDataException(String m) {
        super(m);
    }
}
