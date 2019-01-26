package net.consensys.mahuta.core.domain.indexing;

import java.io.InputStream;
import lombok.Getter;
import lombok.Setter;

public class InputStreamIndexingRequest extends AbstractIndexingRequest {

    private @Getter @Setter InputStream content;

    private InputStreamIndexingRequest() { }
    
    public static InputStreamIndexingRequest build() {
        return new InputStreamIndexingRequest();
    }

    public InputStreamIndexingRequest content(InputStream content) {
        this.setContent(content);
        return this;
    }

}
