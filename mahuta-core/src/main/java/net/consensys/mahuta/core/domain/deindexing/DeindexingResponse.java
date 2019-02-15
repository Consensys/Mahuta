package net.consensys.mahuta.core.domain.deindexing;

import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.core.domain.DefaultResponse;
import net.consensys.mahuta.core.domain.Response;

@Getter @Setter
public class DeindexingResponse extends DefaultResponse implements Response {

    private DeindexingResponse() {
        super(ResponseStatus.SUCCESS);
    }
    
    private DeindexingResponse(ResponseStatus status) {
        super(status);
    }
    
    public static DeindexingResponse of() {
        return of(ResponseStatus.SUCCESS);
    }
    
    public static DeindexingResponse of(ResponseStatus status) {
        return new DeindexingResponse(status);
    }
}
