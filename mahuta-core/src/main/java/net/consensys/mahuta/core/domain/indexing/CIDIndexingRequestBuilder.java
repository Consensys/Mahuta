package net.consensys.mahuta.core.domain.indexing;

import java.util.Map;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class CIDIndexingRequestBuilder extends AbstractBuilder implements Builder<IndexingRequest, IndexingResponse> {

    protected @Getter CIDIndexingRequest request;

    public CIDIndexingRequestBuilder(MahutaService service) {
        super(service);
        request = new CIDIndexingRequest();
    }

    @Override
    public IndexingResponse execute() {
        return service.index(request);
    }

    public CIDIndexingRequestBuilder request(CIDIndexingRequest request) {
        this.request = request;
        return this;
    }

    public CIDIndexingRequestBuilder cid(String cid) {
        request.setCid(cid);
        return this;
    }

    public CIDIndexingRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public CIDIndexingRequestBuilder indexDocId(String indexDocId) {
        request.setIndexDocId(indexDocId);
        return this;
    }

    public CIDIndexingRequestBuilder contentType(String contentType) {
        request.setContentType(contentType);
        return this;
    }

    public CIDIndexingRequestBuilder indexFields(Map<String, Object> indexFields) {
        request.setIndexFields(indexFields);
        return this;
    }

    public CIDIndexingRequestBuilder indexContent(boolean indexContent) {
        request.setIndexContent(indexContent);
        return this;
    }

}
