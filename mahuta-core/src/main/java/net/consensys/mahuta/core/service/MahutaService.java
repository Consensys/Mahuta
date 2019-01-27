package net.consensys.mahuta.core.service;

import java.io.InputStream;
import java.util.List;

import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.deindexing.DeindexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.searching.Query;

public interface MahutaService {
    
    void createIndex(String indexName, InputStream configuration);

    List<String> getIndexes();
    
    Metadata index(IndexingRequest request);
    
    void deindex(DeindexingRequest request);
    
    MetadataAndPayload getByIndexDocId(String index, String indexDocId);
    
    MetadataAndPayload getByContentId(String index, String contentId);
    
    Page<Metadata> search(String index, Query query, PageRequest pageRequest);
    
    Page<MetadataAndPayload> searchAndFetch(String index, Query query, PageRequest pageRequest);
}
