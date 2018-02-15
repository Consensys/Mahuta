package net.consensys.tools.ipfs.ipfsstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class IndexField {

    public IndexField(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    public IndexField(String name, String path) {
        this.name = name;
        this.path = path;
    }


    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("value")
    private Object value;
    
}
