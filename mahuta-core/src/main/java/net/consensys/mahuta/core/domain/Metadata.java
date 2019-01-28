package net.consensys.mahuta.core.domain;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor 
@NoArgsConstructor
@ToString
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

}
