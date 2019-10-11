package net.consensys.mahuta.core.service.pinning.ipfs;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor @NoArgsConstructor
@ToString
public class PinataMetadata {

    @JsonProperty("name")
    private @Getter String name;

    @JsonProperty("keyvalues")
    private Map<String, String> metadata;
    
}
