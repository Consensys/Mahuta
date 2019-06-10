package net.consensys.mahuta.core.domain.indexing;

import lombok.Getter;

import lombok.Setter;

@Getter @Setter
public class CIDIndexingRequest extends AbstractIndexingRequest {
    
    private String cid;
}
