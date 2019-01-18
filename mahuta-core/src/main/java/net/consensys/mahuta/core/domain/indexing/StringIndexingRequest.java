package net.consensys.mahuta.core.domain.indexing;

import lombok.Getter;
import lombok.Setter;

public class StringIndexingRequest  extends AbstractIndexingRequest {
    
    private @Getter @Setter String content;
    
    public static StringIndexingRequest build() {
        return new StringIndexingRequest();
    }

    public StringIndexingRequest content(String content) {
        this.setContent(content);
        return this;
    }
//
//    public StringIndexingRequest indexName(String indexName) {
//        super.setIndexName(indexName);
//        return this;
//    }
//
//    public StringIndexingRequest indexDocId(String indexDocId) {
//        super.setIndexDocId(indexDocId);
//        return this;
//    }
//
//    public StringIndexingRequest contentType(String contentType) {
//        super.setContentType(contentType);
//        return this;
//    }
//
//    public StringIndexingRequest indexFields(Map<String, Object> indexFields) {
//        super.setIndexFields(indexFields);
//        return this;
//    }  
}
