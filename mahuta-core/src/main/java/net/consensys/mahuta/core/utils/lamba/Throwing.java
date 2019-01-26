package net.consensys.mahuta.core.utils.lamba;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public final class Throwing {

    private Throwing() {}

    @Nonnull
    public static <T> Consumer<T> rethrow(@Nonnull final ThrowingConsumer<T> consumer) {
        return consumer;
    }
    @Nonnull
    public static <T1, T2> BiConsumer<T1, T2> rethrow(@Nonnull final ThrowingBiConsumer<T1, T2> consumer) {
        return consumer;
    }

    @Nonnull
    public static <T, R> Function<T, R> rethrowFunc(@Nonnull final ThrowingFunction<T, R> function) {
        return function;
    }

    @Nonnull
    public static <T> Supplier<T> rethrow(@Nonnull final ThrowingSupplier<T> supplier) {
        return supplier;
    }

    /**
     * The compiler sees the signature with the throws T inferred to a RuntimeException type, so it
     * allows the unchecked exception to propagate.
     * 
     * http://www.baeldung.com/java-sneaky-throws
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <E extends Throwable> void sneakyThrow(@Nonnull Throwable ex) throws E {
        throw (E) ex;
    }

}
