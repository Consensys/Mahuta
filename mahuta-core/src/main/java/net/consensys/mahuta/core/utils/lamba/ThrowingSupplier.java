package net.consensys.mahuta.core.utils.lamba;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T> {

    @Override
    default T get() {
        try {
            return get0();
        } catch (Throwable ex) {
            Throwing.sneakyThrow(ex);
            throw new RuntimeException(ex);
        }
    }

    T get0() throws Throwable;

}