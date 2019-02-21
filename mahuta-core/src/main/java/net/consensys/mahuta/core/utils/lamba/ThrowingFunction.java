package net.consensys.mahuta.core.utils.lamba;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T1, R> extends Function<T1, R> {

    @Override
    default R apply(final T1 e1) {
        try {
            return apply0(e1);
        } catch (Throwable ex) {
            Throwing.sneakyThrow(ex);
            throw new RuntimeException(ex);
        }
    }

    R apply0(T1 e1) throws Throwable;

}