package net.consensys.tools.ipfs.ipfsstore.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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
public class Metadata implements Serializable {

    private static final long serialVersionUID = -1081353592236209521L;

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

    public Object getIndexFieldValue(String indexFieldName) {
        if (indexFields == null) {
            return null;
        }
        return indexFields.stream().filter(f -> f.getName().equals(indexFieldName)).findFirst().map(IndexField::getValue).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(index, metadata.index) &&
                Objects.equals(documentId, metadata.documentId) &&
                Objects.equals(hash, metadata.hash) &&
                Objects.equals(contentType, metadata.contentType) &&
                Objects.equals(indexFields, metadata.indexFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index, documentId, hash, contentType, indexFields);
    }
}
