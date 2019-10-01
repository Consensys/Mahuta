package net.consensys.mahuta.springdata.exception;

public class MahutaSpringDataConfigException extends RuntimeException {

    private static final long serialVersionUID = -2583815459528545639L;

    public MahutaSpringDataConfigException(String m, Throwable e) {
        super(m, e);
    }
    
    public MahutaSpringDataConfigException(String m) {
        super(m);
    }
}
