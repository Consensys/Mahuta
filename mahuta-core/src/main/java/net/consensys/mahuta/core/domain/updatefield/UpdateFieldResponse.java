package net.consensys.mahuta.core.domain.updatefield;

import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.core.domain.DefaultResponse;
import net.consensys.mahuta.core.domain.Response;

@Getter @Setter
public class UpdateFieldResponse extends DefaultResponse implements Response {

    private UpdateFieldResponse() {
        super(ResponseStatus.SUCCESS);
    }
    
    private UpdateFieldResponse(ResponseStatus status) {
        super(status);
    }
    
    public static UpdateFieldResponse of() {
        return of(ResponseStatus.SUCCESS);
    }
    
    public static UpdateFieldResponse of(ResponseStatus status) {
        return new UpdateFieldResponse(status);
    }
}
