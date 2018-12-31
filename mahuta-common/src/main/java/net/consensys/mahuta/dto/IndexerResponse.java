package net.consensys.mahuta.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class IndexerResponse {

    @JsonProperty("index")
    private String index;

    @JsonProperty("id")
    private String documentId;

    @JsonProperty("hash")
    private String hash;

}
