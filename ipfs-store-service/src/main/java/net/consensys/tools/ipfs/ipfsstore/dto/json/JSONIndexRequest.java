package net.consensys.tools.ipfs.ipfsstore.dto.json;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;

@Data
@NoArgsConstructor
@ToString
public class JSONIndexRequest extends IndexerRequest {

    private JsonNode payload;
  
}
