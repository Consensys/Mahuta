package net.consensys.mahuta.core.domain.indexing;

import java.io.InputStream;
import java.util.Map;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class InputStreamIndexingRequestBuilder extends AbstractBuilder implements Builder<IndexingRequest, IndexingResponse> {

    protected @Getter InputStreamIndexingRequest request;

    public InputStreamIndexingRequestBuilder(MahutaService service) {
        super(service);
        request = new InputStreamIndexingRequest();
    }

    @Override
    public IndexingResponse execute() {
        return service.index(request);
    }

    public InputStreamIndexingRequestBuilder request(InputStreamIndexingRequest request) {
        this.request = request;
        return this;
    }

    public InputStreamIndexingRequestBuilder content(InputStream content) {
        request.setContent(content);
        return this;
    }

    public InputStreamIndexingRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public InputStreamIndexingRequestBuilder indexDocId(String indexDocId) {
        request.setIndexDocId(indexDocId);
        return this;
    }

    public InputStreamIndexingRequestBuilder contentType(String contentType) {
        request.setContentType(contentType);
        return this;
    }

    public InputStreamIndexingRequestBuilder indexFields(Map<String, Object> indexFields) {
        request.setIndexFields(indexFields);
        return this;
    }

}
