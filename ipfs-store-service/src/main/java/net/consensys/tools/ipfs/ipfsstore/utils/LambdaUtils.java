package net.consensys.tools.ipfs.ipfsstore.utils;

import java.util.function.Function;

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
              throw new RuntimeException(ex);
          }
      };
  }
}
