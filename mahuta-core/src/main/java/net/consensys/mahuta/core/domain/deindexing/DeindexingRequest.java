package net.consensys.mahuta.core.domain.deindexing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.core.domain.Request;

@Getter @Setter @ToString
public class DeindexingRequest implements Request {
    
    private String indexName;
    private String indexDocId;

}
