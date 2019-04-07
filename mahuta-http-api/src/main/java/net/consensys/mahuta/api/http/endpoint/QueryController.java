package net.consensys.mahuta.api.http.endpoint;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest.SortDirection;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.exception.ValidationException;

@RestController
@Slf4j
public class QueryController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final String DEFAULT_PAGE_NO = "0";
    private final Mahuta mahuta;

    @Autowired
    public QueryController(Mahuta mahuta) {
        this.mahuta = mahuta;
    }

    @GetMapping(value = "${mahuta.api-spec.v1.query.fetch}")
    public @ResponseBody ResponseEntity<byte[]> getFile(@PathVariable(value = "hash") @NotNull String hash,
            @RequestParam(value = "index", required = false) String indexName, HttpServletResponse response) {

        // Find and get content by hash
        GetResponse resp;
        try {
            resp = mahuta.prepareGet().indexName(indexName).contentId(hash).loadFile(true).execute();
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (ValidationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
        
        // Attach content-type to the header
        if(resp.getMetadata() != null && resp.getMetadata().getContentType() != null) {
            response.setContentType(resp.getMetadata().getContentType());
        } else {
            response.setContentType("application/octet-stream");
        }
        log.trace("response.getContentType()={}", response.getContentType());

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(response.getContentType()));

        return new ResponseEntity<>(
                ((ByteArrayOutputStream) resp.getPayload()).toByteArray(),
                httpHeaders, 
                HttpStatus.OK);
    }

    @PostMapping(value = "${mahuta.api-spec.v1.query.search}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody SearchResponse searchContentsByPost(
            @RequestParam(value = "index", required = false) String indexName,
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "size", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sort", required = false) Optional<String> sortAttribute,
            @RequestParam(value = "dir", required = false, defaultValue = "ASC") SortDirection sortDirection,
            @RequestBody Query query) {

        PageRequest pageRequest = sortAttribute
                .map(s -> PageRequest.of(pageNo, pageSize, sortAttribute.get(), sortDirection))
                .orElse(PageRequest.of(pageNo, pageSize));

        return mahuta.prepareSearch()
                .indexName(indexName)
                .pageRequest(pageRequest)
                .query(query)
                .execute();
    }

}
