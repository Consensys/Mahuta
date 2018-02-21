package net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.impl;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.IPFSStoreWrapper;
import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreClientException;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.StoreResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;

/**
 * Java REST Wrapper of the IPFS-Store module
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
public class RestIPFSStoreWrapperImpl implements IPFSStoreWrapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestIPFSStoreWrapperImpl.class);

    private static final String BASE_API_PATH = "/ipfs-store";
    private static final String STORE_API_PATH = "/store";
    private static final String INDEX_API_PATH = "/index";
    private static final String FETCH_API_PATH = "/fetchh";
    private static final String SEARCH_API_PATH = "/search";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private final String endpoint;
    
    public RestIPFSStoreWrapperImpl(String endpoint) {
        this.endpoint = endpoint;
        this.restTemplate = new RestTemplate();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    public String store(byte[] file) throws IPFSStoreClientException {

        try {
            LOGGER.debug("store [] ...");
 
            MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
            bodyMap.add("file", new ByteArrayResource(file));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

            ResponseEntity<StoreResponse> response = restTemplate.exchange(
                    this.endpoint+BASE_API_PATH+STORE_API_PATH,
                    HttpMethod.POST, 
                    requestEntity, 
                    StoreResponse.class);
            
            LOGGER.debug("store [] : hash="+response.getBody().getHash());
            
            return response.getBody().getHash();
            
        } catch(RestClientException ex) {
            LOGGER.error("Error while storing the file", ex);
            throw new IPFSStoreClientException("Error while storing the file", ex);
        }
    }

    public IndexerResponse index(IndexerRequest request) throws IPFSStoreClientException {
        try {
            LOGGER.debug("index [request="+request+"] ...");

            HttpHeaders httpHeader = new HttpHeaders();
            httpHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IndexerRequest> httpRequest = new HttpEntity<IndexerRequest>(request, httpHeader);
            
            IndexerResponse response = restTemplate.postForObject(
                    this.endpoint+BASE_API_PATH+INDEX_API_PATH,
                    httpRequest, 
                    IndexerResponse.class);
            

            LOGGER.debug("index [request="+request+"] : "+response);
            
            return response;
            
        } catch(RestClientException ex) {
            LOGGER.error("Error while indexing the content", ex);
            throw new IPFSStoreClientException("Error while indexing the content", ex);
        }
    }

    public byte[] fetch(String indexName, String hash) throws IPFSStoreClientException {
        
        try {
            LOGGER.debug("fetch [indexName="+indexName+", hash="+hash+"] ...");

            restTemplate.getMessageConverters().add(
                    new ByteArrayHttpMessageConverter());

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    this.endpoint+BASE_API_PATH+FETCH_API_PATH+"/"+indexName+"/"+hash,
                    HttpMethod.GET, entity, byte[].class);

            LOGGER.debug("fetch [indexName="+indexName+", hash="+hash+"] : "+response);
            
            return response.getBody();
            
        } catch(RestClientException ex) {
            LOGGER.error("Error while fetching the content", ex);
            throw new IPFSStoreClientException("Error while fetching the content", ex);
        }
    }

    public Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreClientException {
        
        try {
            LOGGER.debug("Search [indexName="+indexName+", query="+query.toString()+"] ...");

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(this.endpoint + SEARCH_API_PATH)
                    .path("/"+indexName)  
                    .queryParam("query", query.toString())
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());
            
            if(pageable.getSort() != null) {
                Order order = pageable.getSort().iterator().next();
                uriComponentsBuilder
                    .queryParam("sort", order.getProperty())
                    .queryParam("dir", order.isAscending()?"ASC":"DESC");
            }
            
            URI url= uriComponentsBuilder.build().encode().toUri(); 
            
            LOGGER.trace("url="+url);
            
            ResponseEntity<Page<Metadata>> response =
                    restTemplate.exchange(url,
                                HttpMethod.GET, null, new ParameterizedTypeReference<Page<Metadata>>() {});

            LOGGER.trace("result"+response.getBody());
            
            LOGGER.debug("Search [indexName="+indexName+", query="+query+"] : " + response.getBody().getTotalElements() + " result(s)");
            
            return response.getBody();
            
        } catch(RestClientException ex) {
            LOGGER.error("Error while searching [indexName="+indexName+", query="+query+"]", ex);
            throw new IPFSStoreClientException("Error while searching  [indexName="+indexName+", query=\"+query+\"]", ex);
        } 
    }

    public RestTemplate getClient() {
        return restTemplate;
    }

}
