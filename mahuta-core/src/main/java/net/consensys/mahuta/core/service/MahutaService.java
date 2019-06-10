package net.consensys.mahuta.core.service;

import net.consensys.mahuta.core.domain.createindex.CreateIndexRequest;
import net.consensys.mahuta.core.domain.createindex.CreateIndexResponse;
import net.consensys.mahuta.core.domain.deindexing.DeindexingRequest;
import net.consensys.mahuta.core.domain.deindexing.DeindexingResponse;
import net.consensys.mahuta.core.domain.get.GetRequest;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesRequest;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.search.SearchRequest;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.domain.updatefield.UpdateFieldRequest;
import net.consensys.mahuta.core.domain.updatefield.UpdateFieldResponse;

public interface MahutaService {
    
    CreateIndexResponse createIndex(CreateIndexRequest request);

    GetIndexesResponse getIndexes(GetIndexesRequest request);

    IndexingResponse index(IndexingRequest request);
    
    UpdateFieldResponse updateField(UpdateFieldRequest request);
    
    DeindexingResponse deindex(DeindexingRequest request);
    
    GetResponse get(GetRequest request);
    
    SearchResponse search(SearchRequest request);
}
