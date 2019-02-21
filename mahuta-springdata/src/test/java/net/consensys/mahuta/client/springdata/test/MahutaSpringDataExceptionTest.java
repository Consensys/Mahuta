package net.consensys.mahuta.client.springdata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.consensys.mahuta.springdata.exception.MahutaSpringDataException;

public class MahutaSpringDataExceptionTest {

    @Test
    public void instantiateException1() {
        String message = "msg";
        MahutaSpringDataException ex = new MahutaSpringDataException(message);
        assertEquals(message, ex.getMessage());
    }
    @Test
    public void instantiateException2() {
        String message = "msg";
        Exception exception = new Exception("my exception");
        MahutaSpringDataException ex = new MahutaSpringDataException(message, exception);
        assertEquals(message, ex.getMessage());
        assertEquals(exception, ex.getCause());
    }
    
}
