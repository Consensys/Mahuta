package net.consensys.tools.ipfs.ipfsstore.endpoint;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.StoreResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.json.JSONIndexRequest;
import net.consensys.tools.ipfs.ipfsstore.exception.TechnicalException;
import net.consensys.tools.ipfs.ipfsstore.exception.ValidationException;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;

@RestController
@Slf4j
public class StoreJSONController {

    private final ObjectMapper mapper;

    private final StoreService storeService;

    @Autowired
    public StoreJSONController(StoreService storeService) {
        this.storeService = storeService;
        this.mapper = new ObjectMapper();
    }


    /**
     * Store a content (any type) on IPFS
     *
     * @param request   Request containing payload
     * @return IPFS hash
     */
    @RequestMapping(value = "${ipfs-store.api-spec.persistence.json.store}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody StoreResponse storeFile(@RequestBody @NotNull JsonNode payload) {

        try {
            return new StoreResponse(this.storeService.storeFile(this.mapper.writeValueAsBytes(payload)));

        } catch (IOException e) {
            log.error("Error in the rest controller", e);
            throw new TechnicalException(e);
        }
    }

    /**
     * Index a content into the search engine
     *
     * @param request Request containing IDs, Hash and metadata
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException 
     */
    @RequestMapping(value = "${ipfs-store.api-spec.persistence.json.index}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexerResponse indexFile(@RequestBody @Valid @NotNull IndexerRequest request) throws ValidationException {

        return this.storeService.indexFile(request);
    }

    /**
     * Store and Index a content
     *
     * @param request   Request containing payload and indexation properties (index, content-type, fields, et.)
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @RequestMapping(value = "${ipfs-store.api-spec.persistence.json.store_index}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexerResponse storeAndIndexFile(@RequestBody @Valid @NotNull JSONIndexRequest request) throws ValidationException  {

        try {
            return this.storeService.storeAndIndexFile(this.mapper.writeValueAsBytes(request.getPayload()), request);

        } catch (IOException e) {
            log.error("Error in the rest controller", e);
            throw new TechnicalException(e);
        }
    }

}
