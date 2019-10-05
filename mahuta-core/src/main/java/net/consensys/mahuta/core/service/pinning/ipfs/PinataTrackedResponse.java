package net.consensys.mahuta.core.service.pinning.ipfs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor @NoArgsConstructor
@ToString
public class PinataTrackedResponse {
	
	private @Getter Long count;
    private @Getter List<PinataTrackedRowResponse> rows;
}
