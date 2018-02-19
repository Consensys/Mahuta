package net.consensys.tools.ipfs.ipfsstore.endpoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.StoreResponse;
import net.consensys.tools.ipfs.ipfsstore.exception.ServiceException;
import net.consensys.tools.ipfs.ipfsstore.query.Query;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;

@RestController
@RequestMapping("${api.base}")
public class StoreController {

    private final Logger LOGGER = LoggerFactory.getLogger(StoreController.class);

    private static final String DEFAULT_PAGE_SIZE = "20";  
    private static final String DEFAULT_PAGE_NO   = "1"; 
    
    private ObjectMapper mapper;
    
    private StoreService storeService;
    
    @Autowired
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
        this.mapper = new ObjectMapper();
    }

    /**
     * Store a content (any type) on IPFS
     * 
     * @param file  File sent as a Multipart
     * @return      IPFS hash
     * 
     * @throws ServiceException
     */
    @RequestMapping(value = "${api.store.uri}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    public @ResponseBody StoreResponse storeFile(
            @RequestParam(value="file", required = true) @Valid @NotNull @NotBlank MultipartFile file) 
                    throws ServiceException {

        try {
            return new StoreResponse(this.storeService.storeFile(file.getBytes()));
            
        } catch (IOException e) {
           LOGGER.error("Error in the rest controller", e);
           throw new ServiceException(e);
        }
    }

    /**
     * Index a content into the search engine
     * 
     * @param request       Request containing IDs, Hash and metadata
     * @return              Response containing the tuple (index, ID, hash)
     * 
     * @throws ServiceException
     */
    @RequestMapping(value = "${api.index.uri}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexerResponse indexFile(
            @RequestBody @Valid @NotNull IndexerRequest request) 
                    throws ServiceException {

        return this.storeService.indexFile(request);
    }
    
    @RequestMapping(value = "${api.store_index.uri}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody IndexerResponse storeAndIndexFile(
            @RequestPart("request") @Valid @NotNull String requestStr, // Spring MVC doesn't support multipart with complex object
            @RequestPart("file") @Valid @NotNull @NotBlank MultipartFile file) 
                    throws ServiceException {

        try {
            IndexerRequest request = mapper.readValue(requestStr, IndexerRequest.class);
            
            return this.storeService.storeAndIndexFile(file.getBytes(), request);
            
        } catch (IOException e) {
           LOGGER.error("Error in the rest controller", e);
           throw new ServiceException(e);
        }     
        
    }

    /**
     * Get content by hash
     * 
     * @param index     Index name
     * @param hash      File Unique Identifier
     * @return          File content
     * 
     * @throws ServiceException
     */
    @RequestMapping(value = "${api.fetch.uri}", method = RequestMethod.GET, produces = MediaType.ALL_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody StreamingResponseBody getFile(
            @PathVariable(value = "index") String index, 
            @PathVariable(value = "hash") String hash, 
            HttpServletResponse response) 
                    throws ServiceException {

        // Search the file in the index by hash
        try {
            Metadata metadata = storeService.getFileMetadataByHash(index, hash);
            response.setContentType(metadata.getContentType());
        } catch(ServiceException notFoundException) {
            response.setContentType("application/octet-stream");
        }
        
        // Get the content in IPFS
        InputStream inputStream = new ByteArrayInputStream(storeService.getFileByHash(hash));
        
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
     * @param index         Index name
     * @param pageNo        Page no [optional - default 1]
     * @param pageSize      Page size [optional - default 20]
     * @param sortAttribute Sorting attribute [optional]
     * @param sortDirection Sorting direction [optional - default ASC]
     * @param query         Query
     * @return              List of result
     * 
     * @throws ServiceException
     */
    @RequestMapping(value = "${api.search.uri}", method = RequestMethod.POST, produces = MediaType.ALL_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Page<Metadata> searchContentsByPost(
            @PathVariable(value = "index") String index, 
            @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NO, required = false) int pageNo,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sort", required = false) String sortAttribute,
            @RequestParam(value = "dir", defaultValue = "ASC", required = false) Sort.Direction sortDirection,
            @RequestBody Query query) 
                    throws ServiceException {
        
        return executeSearch(index, pageNo, pageSize, sortAttribute, sortDirection, query);
    }
    
    /**
     * Search contents By HTTP GET request
     * 
     * @param index         Index name
     * @param pageNo        Page no [optional - default 1]
     * @param pageSize      Page size [optional - default 20]
     * @param sortAttribute Sorting attribute [optional]
     * @param sortDirection Sorting direction [optional - default ASC]
     * @param query         Query
     * @return              List of result
     * 
     * @throws ServiceException
     */
    @RequestMapping(value = "${api.search.uri}", method = RequestMethod.GET, produces = MediaType.ALL_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Page<Metadata> searchContentsByGet(
            @PathVariable(value = "index") String index, 
            @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NO, required = false) int pageNo,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sort", required = false) String sortAttribute,
            @RequestParam(value = "dir", defaultValue = "ASC", required = false) Sort.Direction sortDirection,
            @RequestParam(value = "query", required = false) String queryStr) 
                    throws ServiceException {
        
        try {
            Query query = this.mapper.readValue(queryStr, Query.class);
            
            return executeSearch(index, pageNo, pageSize, sortAttribute, sortDirection, query);
            
        } catch (IOException e) {
           LOGGER.error("Error in the rest controller", e);
           throw new ServiceException(e);
        }  
    }

    private Page<Metadata> executeSearch(String index, int pageNo, int pageSize, String sortAttribute, Sort.Direction sortDirection, Query query) throws ServiceException{
        
        PageRequest pagination = null;
        if(sortAttribute == null || sortAttribute.isEmpty()) {
            pagination = new PageRequest(pageNo, pageSize);
        } else {
            pagination = new PageRequest(pageNo, pageSize, new Sort(sortDirection, sortAttribute));
        }
        
        return this.storeService.searchFiles(index, query, pagination);  
    }
    
    
}
