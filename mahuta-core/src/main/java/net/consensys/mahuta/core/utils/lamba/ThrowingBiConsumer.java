package net.consensys.mahuta.core.utils.lamba;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T1, T2> extends BiConsumer<T1, T2> {

    @Override
    default void accept(final T1 e1, final T2 e2) {
        try {
            accept0(e1, e2);
        } catch (Throwable ex) {
            Throwing.sneakyThrow(ex);
        }
    }

    void accept0(T1 e1, T2 e2) throws Throwable;

}