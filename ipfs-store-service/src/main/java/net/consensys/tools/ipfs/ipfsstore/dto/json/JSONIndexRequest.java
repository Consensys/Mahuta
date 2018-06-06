package net.consensys.tools.ipfs.ipfsstore.dto.json;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;

@Data
@NoArgsConstructor
@ToString
public class JSONIndexRequest extends IndexerRequest {

    @NotNull
    @JsonProperty("payload")
    private JsonNode payload;
  
}
