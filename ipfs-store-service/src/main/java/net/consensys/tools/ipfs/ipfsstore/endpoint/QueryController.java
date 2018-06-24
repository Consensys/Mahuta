package net.consensys.tools.ipfs.ipfsstore.endpoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.exception.TechnicalException;
import net.consensys.tools.ipfs.ipfsstore.exception.TimeoutException;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;
import net.consensys.tools.ipfs.ipfsstore.utils.LambdaUtils;

@RestController
@Slf4j
public class QueryController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final String DEFAULT_PAGE_NO = "0";

    private final ObjectMapper mapper;

    private final StoreService storeService;

    @Autowired
    public QueryController(StoreService storeService) {
        this.storeService = storeService;
        this.mapper = new ObjectMapper();
    }

    /**
     * Get content by hash
     *
     * @param index
     *            Index name
     * @param hash
     *            File Unique Identifier
     * @return File content
     * @throws TimeoutException
     */
    @RequestMapping(value = "${ipfs-store.api-spec.query.fetch}", method = RequestMethod.GET)
    public @ResponseBody StreamingResponseBody getFile(
            @PathVariable(value = "hash") @NotNull String hash,
            @RequestParam(value = "index", required = false) Optional<String> index,
            HttpServletResponse response) throws TimeoutException {

        // Get the content in IPFS
        InputStream inputStream = new ByteArrayInputStream(storeService.getFileByHash(hash));

        // Search the file in the index by hash
        try {
            Metadata metadata = storeService.getFileMetadataByHash(index, hash);
            response.setContentType(metadata.getContentType());
        } catch (NotFoundException e) {
            response.setContentType(guessContentType(inputStream));
        }

        // Send the response as a stream
        return outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };
    }

    /**
     * Search contents By HTTP POST request
     *
     * @param index
     *            Index name
     * @param pageNo
     *            Page no [optional - default 1]
     * @param pageSize
     *            Page size [optional - default 20]
     * @param sortAttribute
     *            Sorting attribute [optional]
     * @param sortDirection
     *            Sorting direction [optional - default ASC]
     * @param query
     *            Query
     * @return List of result
     */
    @RequestMapping(value = "${ipfs-store.api-spec.query.search}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Page<Metadata> searchContentsByPost(
            @RequestParam(value = "index", required = false) Optional<String> index,
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "size", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sort", required = false) Optional<String> sortAttribute,
            @RequestParam(value = "dir", required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestBody Query query) {

        return executeSearch(index, pageNo, pageSize, sortAttribute, sortDirection, query);
    }

    /**
     * Search contents By HTTP GET request
     *
     * @param index
     *            Index name
     * @param pageNo
     *            Page no [optional - default 1]
     * @param pageSize
     *            Page size [optional - default 20]
     * @param sortAttribute
     *            Sorting attribute [optional]
     * @param sortDirection
     *            Sorting direction [optional - default ASC]
     * @param queryStr
     *            Query
     * @return List of result
     */
    @RequestMapping(value = "${ipfs-store.api-spec.query.search}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Page<Metadata> searchContentsByGet(
            @RequestParam(value = "index", required = false) Optional<String> index,
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "size", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sort", required = false) Optional<String> sortAttribute,
            @RequestParam(value = "dir", required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "query", required = false) Optional<String> queryStr) {

        Query query = queryStr
                .map(LambdaUtils
                        .throwingFunctionWrapper(q -> this.mapper.readValue(q, Query.class)))
                .orElse(null);

        return executeSearch(index, pageNo, pageSize, sortAttribute, sortDirection, query);
    }

    /**
     * execute search (common to searchContentsByGet and searchContentsByPost)
     * 
     * @param index
     *            Index name
     * @param pageNo
     *            Page no [optional - default 1]
     * @param pageSize
     *            Page size [optional - default 20]
     * @param sortAttribute
     *            Sorting attribute [optional]
     * @param sortDirection
     *            Sorting direction [optional - default ASC]
     * @param queryStr
     *            Query
     * @return List of result
     */
    private Page<Metadata> executeSearch(Optional<String> index, int pageNo, int pageSize,
            Optional<String> sortAttribute, Sort.Direction sortDirection, Query query) {

        PageRequest pagination = sortAttribute
                .map((s) -> new PageRequest(pageNo, pageSize,
                        new Sort(sortDirection, sortAttribute.get())))
                .orElse(new PageRequest(pageNo, pageSize));

        return this.storeService.searchFiles(index, query, pagination);
    }

    /**
     * Try to guess the content-type from the stream
     * 
     * @param content
     *            inputstream
     * @return Guessed content-type or application/octet-stream
     */
    private static String guessContentType(InputStream content) {

        try {
            String guessedContentType = URLConnection.guessContentTypeFromStream(content);

            if (!StringUtils.isEmpty(guessedContentType)) {
                return guessedContentType;
            } else {
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

        } catch (IOException e) {
            log.error("Unable to guess content type", e);
            throw new TechnicalException("Unable to guess content type", e);
        }
    }

}
