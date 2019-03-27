package net.consensys.mahuta.api.http.endpoint;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.exception.NoIndexException;
import net.consensys.mahuta.core.exception.TechnicalException;

@RestController
@Slf4j
public class IndexController {

    private final Mahuta mahuta;

    @Autowired
    public IndexController(Mahuta mahuta) {
        this.mahuta = mahuta;
    }

    @PostMapping(value = "${mahuta.api-spec.v1.persistence.index.simple}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexingResponse indexFile(@RequestBody @NotNull StringIndexingRequest request) {
        
        try {
            return mahuta.prepareStringIndexing(request).execute();
        } catch (NoIndexException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping(value = "${mahuta.api-spec.v1.persistence.index.cid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexingResponse indexFile(@RequestBody @NotNull CIDIndexingRequest request) {
        
        try {
            return mahuta.prepareCIDndexing(request).execute();
        } catch (NoIndexException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping(value = "${mahuta.api-spec.v1.persistence.index.file}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexingResponse storeAndIndexFile(
            @RequestPart(name = "request")  @NotNull InputStreamIndexingRequest request,
            @RequestPart(name = "file") @Valid @NotNull MultipartFile file) {

        try {
            request.setContent(file.getInputStream());
            return mahuta.prepareInputStreamIndexing(request).execute();

        } catch (NoIndexException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IOException ex) {
            log.error("Error reading the request or the multipart", ex);
            throw new TechnicalException("Error reading the request or the multipart", ex);
        }
    }

}
