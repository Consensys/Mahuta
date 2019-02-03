package net.consensys.mahuta.core.domain.getindexes;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class GetIndexesRequestBuilder extends AbstractBuilder implements Builder<GetIndexesRequest, GetIndexesResponse> {

    protected @Getter GetIndexesRequest request;

    public GetIndexesRequestBuilder(MahutaService service) {
        super(service);
        request = new GetIndexesRequest();
    }

    @Override
    public GetIndexesResponse execute() {
        return service.getIndexes(request);
    }
}
