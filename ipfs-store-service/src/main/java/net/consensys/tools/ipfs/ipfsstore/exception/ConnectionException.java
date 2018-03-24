package net.consensys.tools.ipfs.ipfsstore.exception;

public class ConnectionException extends BaseException {

    private static final long serialVersionUID = 8191498058841215578L;

    /**
     * This constructor will take message String as a parameter and called
     * BaseException class message parameter constructor.
     *
     * @param message is the String text or special message given by user.
     */
    public ConnectionException(String message) {
        super(message);
    }

    /**
     * This constructor will take message String as a parameter and object of
     * Throwable i.e cause and called BaseException class message parameter and
     * throwable object in constructor of BaseException class.
     *
     * @param message is the String text or special message given by user.
     * @param cause is the object of throwable cause
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * This constructor object of Throwable i.e cause as parameter and called
     * BaseException class throwable object in constructor of BaseException
     * class.
     *
     * @param cause is the object of throwable cause
     */
    public ConnectionException(Throwable cause) {
        super(cause);
    }
}
