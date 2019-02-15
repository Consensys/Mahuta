package net.consensys.mahuta.core.domain.indexing;

import java.io.InputStream;

import lombok.Getter;

import lombok.Setter;

@Getter @Setter
public class OnylStoreIndexingRequest extends AbstractIndexingRequest {
    
    private InputStream content;

}
