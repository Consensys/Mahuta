package net.consensys.mahuta.core.domain.get;

import java.io.OutputStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.consensys.mahuta.core.domain.Response;
import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.MetadataAndPayload;

@Getter @Setter
@NoArgsConstructor
public class GetResponse extends MetadataAndPayload implements Response {

    private ResponseStatus status;

    private GetResponse(ResponseStatus status) {
        this.status = status;
    }

    public static GetResponse of() {
        return new GetResponse(ResponseStatus.SUCCESS);
    }

    public static GetResponse of(ResponseStatus status) {
        return of(status);
    }

    public GetResponse metadata(Metadata metadata) {
        this.setMetadata(metadata);
        return this;
    }

    public GetResponse payload(OutputStream payload) {
        this.setPayload(payload);
        return this;
    }

}
