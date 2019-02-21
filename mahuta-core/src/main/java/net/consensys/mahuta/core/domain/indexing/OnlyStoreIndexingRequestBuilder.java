package net.consensys.mahuta.core.domain.indexing;

import java.io.InputStream;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class OnlyStoreIndexingRequestBuilder extends AbstractBuilder implements Builder<IndexingRequest, IndexingResponse> {

    protected @Getter OnylStoreIndexingRequest request;

    public OnlyStoreIndexingRequestBuilder(MahutaService service) {
        super(service);
        request = new OnylStoreIndexingRequest();
    }

    @Override
    public IndexingResponse execute() {
        return service.index(request);
    }

    public OnlyStoreIndexingRequestBuilder request(OnylStoreIndexingRequest request) {
        this.request = request;
        return this;
    }

    public OnlyStoreIndexingRequestBuilder content(InputStream content) {
        request.setContent(content);
        return this;
    }

}
