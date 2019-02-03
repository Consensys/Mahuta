package net.consensys.mahuta.core.domain.indexing;

import lombok.Getter;

import lombok.Setter;
import lombok.ToString;
@Getter @Setter @ToString
public class CIDIndexingRequest  extends AbstractIndexingRequest {
    
    private String cid;

}
