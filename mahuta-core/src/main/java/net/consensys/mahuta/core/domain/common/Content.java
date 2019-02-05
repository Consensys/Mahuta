package net.consensys.mahuta.core.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @NoArgsConstructor
public class Content {

    protected String contentId;

    public static Content of(String contentId) {
        return new Content(contentId);
    }
    
}
