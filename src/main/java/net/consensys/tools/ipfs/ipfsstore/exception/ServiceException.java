/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.consensys.tools.ipfs.ipfsstore.exception;

/**
 * ServiceException is the class which extends BaseException class and used only
 * particular method which is required to us. method with message, cause,
 * (message and cause), and one with no parameter.
 *
 * @author Joshua Cassidy <joshua.cassidy@consensys.net>
 */
public class ServiceException extends BaseException {

    private static final long serialVersionUID = 7333055745689319311L;

    /**
     * This constructor will take message String as a parameter and called
     * BaseException class message parameter constructor.
     *
     * @param message is the String text or special message given by user.
     */
    public ServiceException(String message) {
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
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * This constructor object of Throwable i.e cause as parameter and called
     * BaseException class throwable object in constructor of BaseException
     * class.
     *
     * @param cause is the object of throwable cause
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

}
