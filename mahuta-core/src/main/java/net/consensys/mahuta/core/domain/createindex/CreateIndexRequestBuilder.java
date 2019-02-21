package net.consensys.mahuta.core.domain.createindex;

import java.io.InputStream;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.service.MahutaService;

public class CreateIndexRequestBuilder extends AbstractBuilder implements Builder<CreateIndexRequest, CreateIndexResponse> {

    protected @Getter CreateIndexRequest request;

    public CreateIndexRequestBuilder(MahutaService service) {
        super(service);
        request = new CreateIndexRequest();
    }

    @Override
    public CreateIndexResponse execute() {
        return service.createIndex(request);
    }

    public CreateIndexRequestBuilder name(String name) {
        request.setName(name);
        return this;
    }

    public CreateIndexRequestBuilder configuration(InputStream configuration) {
        request.setConfiguration(configuration);
        return this;
    }
}
