package net.consensys.mahuta.core.domain.indexing;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractIndexingRequest implements IndexingRequest {

    protected @Getter @Setter String indexName;

    protected @Getter @Setter String indexDocId;

    protected @Getter @Setter String contentType;

    protected @Getter @Setter Map<String, Object> indexFields;

    public AbstractIndexingRequest indexName(String indexName) {
        this.setIndexName(indexName);
        return this;
    }

    public AbstractIndexingRequest indexDocId(String indexDocId) {
        this.setIndexDocId(indexDocId);
        return this;
    }

    public AbstractIndexingRequest contentType(String contentType) {
        this.setContentType(contentType);
        return this;
    }

    public AbstractIndexingRequest indexFields(Map<String, Object> indexFields) {
        this.setIndexFields(indexFields);
        return this;
    }

}
