package net.consensys.tools.ipfs.ipfsstore.client.cli.model;

public class ClientShellException extends Exception {

    private static final long serialVersionUID = 8288267165614432072L;
    
    public ClientShellException(String message) {
        super(message);
    }
    public ClientShellException(String message, Throwable e) {
        super(message, e);
    }

}
