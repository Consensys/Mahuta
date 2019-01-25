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
}
