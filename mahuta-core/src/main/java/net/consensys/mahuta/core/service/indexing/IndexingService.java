package net.consensys.mahuta.core.service.indexing;

import java.io.InputStream;
import java.util.Map;

import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.searching.Query;

public interface IndexingService {

    static final String HASH_INDEX_KEY = "__hash";
    static final String CONTENT_TYPE_INDEX_KEY = "__content_type";
    
    void createIndex(String indexName);
    
    void createIndex(String indexName, InputStream configuration);
    
    String index(String indexName, String indexDocId, String contentId, String contentType, Map<String, Object> indexFields);
    
    void deindex(String indexName, String indexDocId);
    
    Metadata getDocument(String indexName, String indexDocId);
    
    Page<Metadata> searchDocuments(String index, Query query, PageRequest pageRequest);
}
