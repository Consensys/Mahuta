package net.consensys.mahuta.core.service.indexing;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;

public interface IndexingService {

    static final String HASH_INDEX_KEY = "__hash";
    static final String CONTENT_TYPE_INDEX_KEY = "__content_type";
    
    void createIndex(String indexName);
    
    void createIndex(String indexName, InputStream configuration);
    
    List<String> getIndexes();
    
    String index(String indexName, String indexDocId, String contentId, String contentType, Map<String, Object> indexFields);
    
    void deindex(String indexName, String indexDocId);
    
    Metadata getDocument(String indexName, String indexDocId);
    
    Page<Metadata> searchDocuments(String index, Query query, PageRequest pageRequest);
}
