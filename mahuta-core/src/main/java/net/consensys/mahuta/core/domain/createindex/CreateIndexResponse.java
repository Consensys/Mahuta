package net.consensys.mahuta.core.domain.createindex;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.core.domain.DefaultResponse;
import net.consensys.mahuta.core.domain.Response;

@Getter @Setter @ToString
public class CreateIndexResponse extends DefaultResponse implements Response {

    private CreateIndexResponse() {
        super(ResponseStatus.SUCCESS);
    }
    
    private CreateIndexResponse(ResponseStatus status) {
        super(status);
    }
    
    public static CreateIndexResponse of() {
        return new CreateIndexResponse(ResponseStatus.SUCCESS);
    }
    
    public static CreateIndexResponse of(ResponseStatus status) {
        return new CreateIndexResponse(status);
    }
}
