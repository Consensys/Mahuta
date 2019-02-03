package net.consensys.mahuta.core.domain.search;

import lombok.Getter;
import net.consensys.mahuta.core.domain.AbstractBuilder;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.service.MahutaService;

public class SearchRequestBuilder extends AbstractBuilder implements Builder<SearchRequest, SearchResponse> {

    protected @Getter SearchRequest request;

    public SearchRequestBuilder(MahutaService service) {
        super(service);
        request = new SearchRequest();
        request.setLoadFile(false);
        request.setPageRequest(PageRequest.of());
        request.setQuery(Query.newQuery());
    }

    @Override
    public SearchResponse execute() {
        return service.search(request);
    }

    public SearchRequestBuilder pageRequest(PageRequest pageRequest) {
        request.setPageRequest(pageRequest);
        return this;
    }

    public SearchRequestBuilder query(Query query) {
        request.setQuery(query);
        return this;
    }
    
    public SearchRequestBuilder indexName(String indexName) {
        request.setIndexName(indexName);
        return this;
    }

    public SearchRequestBuilder loadFile(boolean loadFile) {
        request.setLoadFile(loadFile);
        return this;
    }
}
