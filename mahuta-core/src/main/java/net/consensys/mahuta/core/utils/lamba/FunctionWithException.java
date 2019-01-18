package net.consensys.mahuta.core.utils.lamba;

import java.util.function.Function;

import net.consensys.mahuta.core.exception.TechnicalException;

@FunctionalInterface
public interface FunctionWithException<T, R, E extends Exception> {

    R apply(T t) throws E;
    
    static <T, R, E extends Exception> Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new TechnicalException(e);
            }
        };
    }
}