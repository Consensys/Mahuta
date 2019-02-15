package net.consensys.mahuta.core.domain.get;

import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.core.domain.Request;

@Getter @Setter
public class GetRequest implements Request {
    
    private String indexName;
    private String indexDocId;
    private String contentId;
    private boolean loadFile;

}
