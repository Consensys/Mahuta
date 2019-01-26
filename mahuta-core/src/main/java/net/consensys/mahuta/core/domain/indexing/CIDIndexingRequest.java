package net.consensys.mahuta.core.domain.indexing;

import lombok.Getter;
import lombok.Setter;

public class CIDIndexingRequest  extends AbstractIndexingRequest {
    
    private @Getter @Setter String cid;

    public static CIDIndexingRequest build() {
        return new CIDIndexingRequest();
    }
    
    public CIDIndexingRequest content(String cid) {
        this.setCid(cid);
        return this;
    }

}
