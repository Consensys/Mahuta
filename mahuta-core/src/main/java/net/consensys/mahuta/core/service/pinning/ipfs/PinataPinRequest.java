package net.consensys.mahuta.core.service.pinning.ipfs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor @NoArgsConstructor
@ToString
public class PinataPinRequest {

    @JsonProperty("hashToPin")
    private @Getter String hash;
    
    @JsonProperty("host_nodes")
    private @Getter List<String> hostNodes;
    
    @JsonProperty("pinataMetadata")
    private PinataMetadata metadata;
    
}
