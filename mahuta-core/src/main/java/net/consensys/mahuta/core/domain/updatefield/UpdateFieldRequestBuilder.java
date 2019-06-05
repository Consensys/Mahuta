package net.consensys.mahuta.core.domain.updatefield;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class UpdateFieldRequestBuilder extends AbstractBuilder implements Builder<UpdateFieldRequest, UpdateFieldResponse> {

    protected @Getter UpdateFieldRequest request;

    public UpdateFieldRequestBuilder(MahutaService service) {
        super(service);
        request = new UpdateFieldRequest();
    }

    @Override
    public UpdateFieldResponse execute() {
        return service.updateField(request);
    }

    public UpdateFieldRequestBuilder request(UpdateFieldRequest request) {
        this.request = request;
        return this;
    }

    public UpdateFieldRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public UpdateFieldRequestBuilder indexDocId(String indexDocId) {
        request.setIndexDocId(indexDocId);
        return this;
    }

    public UpdateFieldRequestBuilder key(String key) {
        request.setKey(key);
        return this;
    }

    public UpdateFieldRequestBuilder value(Object value) {
        request.setValue(value);
        return this;
    }
}
