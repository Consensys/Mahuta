package net.consensys.mahuta.core.domain.deindexing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.core.domain.DefaultResponse;
import net.consensys.mahuta.core.domain.Response;

@Getter @Setter @ToString
public class DeindexingResponse extends DefaultResponse implements Response {

    private DeindexingResponse() {
        super(ResponseStatus.SUCCESS);
    }
    
    private DeindexingResponse(ResponseStatus status) {
        super(status);
    }
    
    public static DeindexingResponse of() {
        return new DeindexingResponse(ResponseStatus.SUCCESS);
    }
    
    public static DeindexingResponse of(ResponseStatus status) {
        return new DeindexingResponse(status);
    }
}
