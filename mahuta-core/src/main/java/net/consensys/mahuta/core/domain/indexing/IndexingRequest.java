package net.consensys.mahuta.core.domain.indexing;

import java.util.Map;

import net.consensys.mahuta.core.domain.Request;

public interface IndexingRequest extends Request {

    String getIndexName();
    
    void setIndexName(String indexName);

    String getIndexDocId();
    
    void setIndexDocId(String indexDocId);
    
    String getContentType();
    
    void setContentType(String contentType);
    
    Map<String, Object> getIndexFields();
    
    void setIndexFields(Map<String, Object> indexName);
}
