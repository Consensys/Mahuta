package net.consensys.mahuta.core.domain.indexing;

import java.util.Map;

public interface IndexingRequest {

    String getIndexName();

    String getIndexDocId();
    
    String getContentType();
    
    Map<String, Object> getIndexFields();
}
