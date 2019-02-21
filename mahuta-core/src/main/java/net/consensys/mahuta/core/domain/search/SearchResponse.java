package net.consensys.mahuta.core.domain.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.consensys.mahuta.core.domain.Response;
import net.consensys.mahuta.core.domain.common.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.pagination.Page;

@Getter @Setter
@NoArgsConstructor
public class SearchResponse implements Response {

    private ResponseStatus status;
    private Page<MetadataAndPayload> page;

    private SearchResponse(ResponseStatus status) {
        this.status = status;
    }

    public static SearchResponse of() {
        return of(ResponseStatus.SUCCESS);
    }

    public static SearchResponse of(ResponseStatus status) {
        return new SearchResponse(status);
    }

    public SearchResponse result(Page<MetadataAndPayload> page) {
        this.setPage(page);
        return this;
    }
}
