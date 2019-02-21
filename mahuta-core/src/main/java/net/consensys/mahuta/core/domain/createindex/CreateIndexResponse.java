package net.consensys.mahuta.core.domain.createindex;

import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.core.domain.DefaultResponse;
import net.consensys.mahuta.core.domain.Response;

@Getter @Setter
public class CreateIndexResponse extends DefaultResponse implements Response {

    private CreateIndexResponse() {
        this(ResponseStatus.SUCCESS);
    }
    
    private CreateIndexResponse(ResponseStatus status) {
        super(status);
    }
    
    public static CreateIndexResponse of() {
        return of(ResponseStatus.SUCCESS);
    }
    
    public static CreateIndexResponse of(ResponseStatus status) {
        return new CreateIndexResponse(status);
    }
}
