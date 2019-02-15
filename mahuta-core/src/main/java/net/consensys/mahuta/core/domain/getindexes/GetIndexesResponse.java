package net.consensys.mahuta.core.domain.getindexes;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.core.domain.Response;

@Getter @Setter @ToString
@NoArgsConstructor
public class GetIndexesResponse implements Response {

    private ResponseStatus status;
    private List<String> indexes;
    
    private GetIndexesResponse(ResponseStatus status) {
        this.status = status;
    }
    
    public static GetIndexesResponse of() {
        return of(ResponseStatus.SUCCESS);
    }
    
    public static GetIndexesResponse of(ResponseStatus status) {
        return new GetIndexesResponse(status);
    }
    
    public GetIndexesResponse indexes(List<String> indexes) {
        this.indexes = indexes;
        return this;
    }

}
