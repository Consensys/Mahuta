package net.consensys.mahuta.client.springdata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.consensys.mahuta.springdata.exception.MahutaSpringDataConfigException;
import net.consensys.mahuta.springdata.exception.MahutaSpringDataRuntimeException;

public class MahutaSpringDataExceptionTest {

    @Test
    public void instantiateException1() {
        String message = "msg";
        MahutaSpringDataConfigException ex = new MahutaSpringDataConfigException(message);
        assertEquals(message, ex.getMessage());
    }
    @Test
    public void instantiateException2() {
        String message = "msg";
        Exception exception = new Exception("my exception");
        MahutaSpringDataConfigException ex = new MahutaSpringDataConfigException(message, exception);
        assertEquals(message, ex.getMessage());
        assertEquals(exception, ex.getCause());
    }
    @Test
    public void instantiateException3() {
        String message = "msg";
        MahutaSpringDataRuntimeException ex = new MahutaSpringDataRuntimeException(message);
        assertEquals(message, ex.getMessage());
    }
    @Test
    public void instantiateException4() {
        String message = "msg";
        Exception exception = new Exception("my exception");
        MahutaSpringDataRuntimeException ex = new MahutaSpringDataRuntimeException(message, exception);
        assertEquals(message, ex.getMessage());
        assertEquals(exception, ex.getCause());
    }
    
}
