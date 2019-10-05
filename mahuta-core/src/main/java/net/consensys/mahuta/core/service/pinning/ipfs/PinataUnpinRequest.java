package net.consensys.mahuta.core.service.pinning.ipfs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor @NoArgsConstructor
@ToString
public class PinataUnpinRequest {

    @JsonProperty("ipfs_pin_hash")
    private @Getter String h;
    
    
    
}
