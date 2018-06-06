package net.consensys.tools.ipfs.ipfsstore.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexerRequest {

    @NotNull
    @JsonProperty("index")
    private String index;

    @JsonProperty("id")
    private String documentId;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("index_fields")
    private List<IndexField> indexFields;


}
