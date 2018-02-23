package net.consensys.tools.ipfs.ipfsstore.client.java.exception;

/**
 * IPFSStoreException
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
public class IPFSStoreException extends Exception {

    private static final long serialVersionUID = 3153727787764151573L;

    /**
     * This constructor will not take any parameter it called Exception class
     * default constructor.
     */
    public IPFSStoreException() {
        super();
    }

    /**
     * This constructor will take message String as a parameter and called Exception
     * class message parameter constructor.
     *
     * @param message
     *            is the String text or special message given by user.
     */
    public IPFSStoreException(String message) {
        super(message);
    }

    /**
     * This constructor will take message String as a parameter and object of
     * Throwable i.e cause and called Exception class message parameter and
     * throwable object in constructor of Exception class.
     *
     * @param message
     *            is the String text or special message given by user.
     * @param cause
     *            is the object of throwable cause
     */
    public IPFSStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * This constructor object of Throwable i.e cause as parameter and called
     * Exception class throwable object in constructor of Exception class.
     *
     * @param cause
     *            is the object of throwable cause
     */
    public IPFSStoreException(Throwable cause) {
        super(cause);
    }

}