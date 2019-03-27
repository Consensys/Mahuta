package net.consensys.mahuta.core.exception;

public class NoIndexException extends MahutaException {

    private static final long serialVersionUID = -1577067446504139812L;
    private static final String messageFormat = "Index %s does not exist. Please create one using the operation 'create_index'";
    
    public NoIndexException(String indexName) {
        super(String.format(messageFormat, indexName));
    }
}
