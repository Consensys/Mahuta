package net.consensys.mahuta.utils;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.exception.TechnicalException;

@Slf4j
public class LambdaUtils {

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public static <T, R> Function<T, R> throwingFunctionWrapper(
            ThrowingFunction<T, R, Exception> throwingConsumer) {

        return i -> {
            try {
                return throwingConsumer.apply(i);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new TechnicalException(ex);
            }
        };
    }
}
