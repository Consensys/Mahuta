package net.consensys.mahuta.core.domain.updatefield;

import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.core.domain.Request;

@Getter @Setter
public class UpdateFieldRequest implements Request {
    
    protected String indexName;
    protected String indexDocId;
    protected String key;
    protected Object value;

}
