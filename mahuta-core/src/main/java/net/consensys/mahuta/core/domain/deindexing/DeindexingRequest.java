package net.consensys.mahuta.core.domain.deindexing;

import lombok.Getter;
import lombok.Setter;

public class DeindexingRequest {
    
    private @Getter @Setter String indexName;

    private @Getter @Setter String id;
    
    private DeindexingRequest(String indexName, String id) {
        this.indexName = indexName;
        this.id = id;
    }
    
    public static DeindexingRequest of(String indexName, String id) {
        return new DeindexingRequest(indexName, id);
    }
}
