package net.consensys.mahuta.core.domain;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @ToString
public class Metadata {

    private @Getter String indexName;
    private @Getter String indexDocId;
    private @Getter String contentId;
    private @Getter String contentType;
    private @Getter Map<String, Object> indexFields;

    public static Metadata of(String indexName, String indexDocId, String contentId, String contentType,
            Map<String, Object> indexFields) {
        return new Metadata(indexName, indexDocId, contentId, contentType, indexFields);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contentId == null) ? 0 : contentId.hashCode());
        result = prime * result + ((indexDocId == null) ? 0 : indexDocId.hashCode());
        result = prime * result + ((indexName == null) ? 0 : indexName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Metadata other = (Metadata) obj;
        if (contentId == null) {
            if (other.contentId != null)
                return false;
        } else if (!contentId.equals(other.contentId))
            return false;
        if (indexDocId == null) {
            if (other.indexDocId != null)
                return false;
        } else if (!indexDocId.equals(other.indexDocId))
            return false;
        if (indexName == null) {
            if (other.indexName != null)
                return false;
        } else if (!indexName.equals(other.indexName))
            return false;
        return true;
    }

}
