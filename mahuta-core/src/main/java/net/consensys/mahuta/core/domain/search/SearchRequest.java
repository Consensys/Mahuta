package net.consensys.mahuta.core.domain.search;

import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.core.domain.Request;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;

@Getter @Setter
public class SearchRequest implements Request {
    
    private String indexName;
    private Query query;
    private PageRequest pageRequest;
    private boolean loadFile;
}
