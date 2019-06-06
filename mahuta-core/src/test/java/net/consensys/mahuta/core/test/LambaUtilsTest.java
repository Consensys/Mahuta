package net.consensys.mahuta.core.test;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import net.consensys.mahuta.core.utils.lamba.Throwing;

public class LambaUtilsTest {

    private class MyException extends Exception {
        private static final long serialVersionUID = 2567136282225703533L;
    }

    @Test(expected=MyException.class)
    public void function() {
        String str= "hello";
        
        Optional.of(str).map(Throwing.rethrowFunc(s -> {throw new MyException();}));
    }

    @Test(expected = MyException.class)
    public void consumer() {
        String str = "hello";

        Optional.of(str).ifPresent(Throwing.rethrowConsumer(s -> {throw new MyException();}));
    }

    @Test(expected = MyException.class)
    public void supplier() {
        String str = null;

        Optional.ofNullable(str).orElseGet(Throwing.rethrowSupplier(() -> {throw new MyException();}));
    }

    @Test(expected = MyException.class)
    public void biconsumer() {
        Map<String, String> mymap = ImmutableMap.of("hello", "world");

        mymap.forEach(Throwing.rethrowBiConsumer((k,v) -> {throw new MyException();}));
    }

}
