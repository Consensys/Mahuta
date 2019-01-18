package net.consensys.mahuta.core.domain.indexing;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

public class JSONIndexingRequest  extends AbstractIndexingRequest {

    private @Getter @Setter JsonNode content;
    
    public static JSONIndexingRequest build() {
        return new JSONIndexingRequest();
    }

    public JSONIndexingRequest content(JsonNode content) {
        this.setContent(content);
        return this;
    }

}
