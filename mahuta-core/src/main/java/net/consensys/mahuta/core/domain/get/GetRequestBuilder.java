package net.consensys.mahuta.core.domain.get;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class GetRequestBuilder extends AbstractBuilder implements Builder<GetRequest, GetResponse> {

    protected @Getter GetRequest request;

    public GetRequestBuilder(MahutaService service) {
        super(service);
        request = new GetRequest();
        request.setLoadFile(false);
    }

    @Override
    public GetResponse execute() {
        return service.get(request);
    }

    public GetRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public GetRequestBuilder indexDocId(String indexDocId) {
        request.setIndexDocId(indexDocId);
        return this;
    }

    public GetRequestBuilder contentId(String contentId) {
        request.setContentId(contentId);
        return this;
    }

    public GetRequestBuilder loadFile(boolean loadFile) {
        request.setLoadFile(loadFile);
        return this;
    }
}
