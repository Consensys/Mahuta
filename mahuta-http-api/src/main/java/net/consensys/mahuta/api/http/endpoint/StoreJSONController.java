package net.consensys.mahuta.api.http.endpoint;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;

@RestController
public class StoreJSONController {

    private final Mahuta mahuta;

    @Autowired
    public StoreJSONController(Mahuta mahuta) {
        this.mahuta = mahuta;
    }

    /**
     * Index a content into the search engine
     *
     * @param request Request containing IDs, Hash and metadata
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @PostMapping(value = "${mahuta.api-spec.v1.persistence.json.index}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Metadata indexFile(@RequestBody @Valid @NotNull CIDIndexingRequest request) {

        return mahuta.index(request);
    }

    /**
     * Store and Index a content
     *
     * @param request Request containing payload and indexation properties (index,
     *                content-type, fields, et.)
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @PostMapping(value = "${mahuta.api-spec.v1.persistence.json.store_index}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Metadata storeAndIndexFile(@RequestBody @Valid @NotNull StringIndexingRequest request) {

        return mahuta.index(request);
    }

}
