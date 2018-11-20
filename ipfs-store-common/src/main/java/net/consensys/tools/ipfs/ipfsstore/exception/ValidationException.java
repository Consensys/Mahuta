package net.consensys.tools.ipfs.ipfsstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends IPFSStoreException {

    private static final long serialVersionUID = -1577067446504139812L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

}
