package net.consensys.mahuta.core.domain.indexing;

import lombok.Getter;

import lombok.Setter;
import net.consensys.mahuta.core.domain.Request;

@Getter @Setter
public class StringIndexingRequest  extends AbstractIndexingRequest implements Request {
    
    private String content;

}
