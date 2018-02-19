package net.consensys.tools.ipfs.ipfsstore.dto;

import java.util.List;

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
public class Metadata {

    @JsonProperty("index")
    private String indexName;
    
    @JsonProperty("id")
    private String documentId;
    
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("content_type")
    private String contentType;
    
    @JsonProperty("index_fields")
    private List<IndexField> indexFields;
    
    public Object getIndexFieldValue(String indexFieldName) {
        if(indexFields == null) {
            return null;
        }
        return indexFields.stream().filter(f->f.getName()==indexFieldName).findFirst().map(f->f.getValue()).orElse(null);
    }
}
