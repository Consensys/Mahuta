package net.consensys.tools.ipfs.ipfsstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
public class TimeoutException extends Exception {

    private static final long serialVersionUID = -1577067446504139812L;

    public TimeoutException(String message) {
        super(message);
    }
    
    public TimeoutException(String messageFormat, Object... args) {
      super(String.format(messageFormat, args));
  }

}
