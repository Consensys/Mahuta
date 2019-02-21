package net.consensys.mahuta.core.domain;

public interface Builder<R extends Request, S extends Response> {

    R getRequest();
    
    S execute();
    
}
