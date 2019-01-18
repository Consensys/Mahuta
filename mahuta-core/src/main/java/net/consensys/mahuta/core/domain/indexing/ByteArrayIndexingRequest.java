package net.consensys.mahuta.core.domain.indexing;

import lombok.Getter;
import lombok.Setter;

public class ByteArrayIndexingRequest extends AbstractIndexingRequest {

    private @Getter @Setter byte[] content;

    private ByteArrayIndexingRequest() { }
    
    public static ByteArrayIndexingRequest build() {
        return new ByteArrayIndexingRequest();
    }

    public ByteArrayIndexingRequest content(byte[] content) {
        this.setContent(content);
        return this;
    }

}
