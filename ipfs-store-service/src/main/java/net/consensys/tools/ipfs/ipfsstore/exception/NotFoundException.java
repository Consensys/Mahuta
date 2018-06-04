/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.consensys.tools.ipfs.ipfsstore.exception;

/**
 * @author Joshua Cassidy <joshua.cassidy@consensys.net>
 */
public class NotFoundException extends BaseException {

    private static final long serialVersionUID = -1577067446504139812L;

    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String messageFormat, Object... args) {
      super(String.format(messageFormat, args));
  }

}
