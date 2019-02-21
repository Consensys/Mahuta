package net.consensys.mahuta.core.domain.deindexing;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class DeindexingRequestBuilder extends AbstractBuilder implements Builder<DeindexingRequest, DeindexingResponse> {

    protected @Getter DeindexingRequest request;

    public DeindexingRequestBuilder(MahutaService service) {
        super(service);
        request = new DeindexingRequest();
    }

    @Override
    public DeindexingResponse execute() {
        return service.deindex(request);
    }

    public DeindexingRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public DeindexingRequestBuilder indexDocId(String indexDocId) {
        request.setIndexDocId(indexDocId);
        return this;
    }
}
