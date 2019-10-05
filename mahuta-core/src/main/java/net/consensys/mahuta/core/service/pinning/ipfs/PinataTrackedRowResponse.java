package net.consensys.mahuta.core.service.pinning.ipfs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor @NoArgsConstructor
@ToString
public class PinataTrackedRowResponse {
	
	@JsonProperty("id")
	private @Getter String id;
	
	@JsonProperty("ipfs_pin_hash")
	private @Getter String hash;
    
}
