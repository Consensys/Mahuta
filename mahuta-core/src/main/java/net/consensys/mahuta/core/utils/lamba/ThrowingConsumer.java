package net.consensys.mahuta.core.utils.lamba;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T1> extends Consumer<T1> {

    @Override
    default void accept(final T1 e1) {
        try {
            accept0(e1);
        } catch (Throwable ex) {
            Throwing.sneakyThrow(ex);
        }
    }

    void accept0(T1 e1) throws Throwable;

}