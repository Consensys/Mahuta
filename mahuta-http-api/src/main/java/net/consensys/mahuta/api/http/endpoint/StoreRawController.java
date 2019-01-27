package net.consensys.mahuta.api.http.endpoint;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.exception.TechnicalException;

@RestController
@Slf4j
public class StoreRawController {

    private final Mahuta mahuta;
    private final ObjectMapper mapper;

    @Autowired
    public StoreRawController(Mahuta mahuta) {
        this.mahuta = mahuta;
        this.mapper = new ObjectMapper();
    }

    /**
     * Index a content into the search engine
     *
     * @param request Request containing IDs, Hash and metadata
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @PostMapping(value = "${mahuta.api-spec.v1.persistence.raw.index}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Metadata indexFile(@RequestBody @Valid CIDIndexingRequest request) {

        return mahuta.index(request);
    }

    /**
     * Store and Index a content
     *
     * @param requestStr Request containing IDs, Hash and metadata
     * @param file       File sent as a Multipart
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @PostMapping(value = "${mahuta.api-spec.v1.persistence.raw.store_index}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Metadata storeAndIndexFile(@RequestPart(name = "request") @Valid @NotNull String requestStr,
            @RequestPart(name = "file") @Valid @NotNull MultipartFile file) {

        try {
            InputStreamIndexingRequest request = mapper.readValue(requestStr, InputStreamIndexingRequest.class);
            request.setContent(file.getInputStream());

            return mahuta.index(request);

        } catch (IOException ex) {
            log.error("Error reading the request or the multipart", ex);
            throw new TechnicalException("Error reading the request or the multipart", ex);
        }
    }

}
