package net.consensys.mahuta.core.domain.indexing;

import java.util.Map;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class StringIndexingRequestBuilder extends AbstractBuilder implements Builder<IndexingRequest, IndexingResponse> {

    protected @Getter StringIndexingRequest request;

    public StringIndexingRequestBuilder(MahutaService service) {
        super(service);
        request = new StringIndexingRequest();
    }

    @Override
    public IndexingResponse execute() {
        return service.index(request);
    }

    public StringIndexingRequestBuilder request(StringIndexingRequest request) {
        this.request = request;
        return this;
    }

    public StringIndexingRequestBuilder content(String content) {
        request.setContent(content);
        return this;
    }

    public StringIndexingRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public StringIndexingRequestBuilder indexDocId(String indexDocId) {
        request.setIndexDocId(indexDocId);
        return this;
    }

    public StringIndexingRequestBuilder contentType(String contentType) {
        request.setContentType(contentType);
        return this;
    }

    public StringIndexingRequestBuilder indexFields(Map<String, Object> indexFields) {
        request.setIndexFields(indexFields);
        return this;
    }

    public StringIndexingRequestBuilder indexContent(boolean indexContent) {
        request.setIndexContent(indexContent);
        return this;
    }

}
