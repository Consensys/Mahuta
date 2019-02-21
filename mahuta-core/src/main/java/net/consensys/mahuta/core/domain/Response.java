package net.consensys.mahuta.core.domain;

public interface Response {
    
    public enum ResponseStatus { SUCCESS, ERROR}

    ResponseStatus getStatus();
}
