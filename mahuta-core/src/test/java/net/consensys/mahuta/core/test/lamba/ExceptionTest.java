package net.consensys.mahuta.core.test.lamba;

import org.junit.Test;

import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.MahutaException;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.exception.ValidationException;

public class ExceptionTest {

    private static final String MESSAGE = "message";
    private static final String MESSAGE_FORMAT = "message %s";
    
    @Test(expected=MahutaException.class)
    public void MahutaException() {
        throw new MahutaException(MESSAGE);
    }
    @Test(expected=NotFoundException.class)
    public void NotFoundException() {
        throw new NotFoundException(MESSAGE_FORMAT, "hello");
    }
    @Test(expected=TimeoutException.class)
    public void TimeoutException() {
        throw new TimeoutException(MESSAGE_FORMAT, "hello");
    }
    @Test(expected=ValidationException.class)
    public void ValidationException() {
        throw new ValidationException(MESSAGE_FORMAT, "hello");
    }
    @Test(expected=TechnicalException.class)
    public void TechnicalException() {
        throw new TechnicalException(MESSAGE, new Exception());
    }
    @Test(expected=ConnectionException.class)
    public void ConnectionException() {
        throw new ConnectionException(MESSAGE, new Exception());
    }
    
}
