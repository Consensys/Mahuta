package net.consensys.tools.ipfs.ipfsstore.endpoint;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.StoreResponse;
import net.consensys.tools.ipfs.ipfsstore.exception.TechnicalException;
import net.consensys.tools.ipfs.ipfsstore.exception.ValidationException;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;

@RestController
@Slf4j
public class StoreRawController {

    private final ObjectMapper mapper;

    private final StoreService storeService;

    @Autowired
    public StoreRawController(StoreService storeService) {
        this.storeService = storeService;
        this.mapper = new ObjectMapper();
    }

    /**
     * Store a content (any type) on IPFS
     *
     * @param file
     *            File sent as a Multipart
     * @return IPFS hash
     */
    @RequestMapping(value = "${ipfs-store.api-spec.persistence.raw.store}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody StoreResponse storeFile(
            @RequestParam(value = "file") @Valid @NotNull MultipartFile file) {

        try {
            return new StoreResponse(this.storeService.storeFile(file.getBytes()));

        } catch (IOException e) {
            log.error("Error in the rest controller", e);
            throw new TechnicalException(e);
        }
    }

    /**
     * Index a content into the search engine
     *
     * @param request
     *            Request containing IDs, Hash and metadata
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @RequestMapping(value = "${ipfs-store.api-spec.persistence.raw.index}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexerResponse indexFile(@RequestBody @Valid IndexerRequest request)
            throws ValidationException {

        return this.storeService.indexFile(request);
    }

    /**
     * Store and Index a content
     *
     * @param requestStr
     *            Request containing IDs, Hash and metadata
     * @param file
     *            File sent as a Multipart
     * @return Response containing the tuple (index, ID, hash)
     * @throws ValidationException
     */
    @RequestMapping(value = "${ipfs-store.api-spec.persistence.raw.store_index}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexerResponse storeAndIndexFile(
            @RequestPart(name = "request") @Valid @NotNull String requestStr,
            @RequestPart(name = "file") @Valid @NotNull @NotBlank MultipartFile file)
            throws ValidationException {

        try {
            IndexerRequest request = mapper.readValue(requestStr, IndexerRequest.class);

            return this.storeService.storeAndIndexFile(file.getBytes(), request);

        } catch (IOException e) {
            log.error("Error in the rest controller", e);
            throw new TechnicalException(e);
        }
    }

}
