package net.consensys.mahuta.core.domain.indexing;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class AbstractIndexingRequest implements IndexingRequest {

    protected String indexName;
    protected String indexDocId;
    protected String contentType;
    protected Map<String, Object> indexFields;
}
