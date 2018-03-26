package net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.impl;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.java.utils.RestResponsePage;
import net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.IPFSStoreWrapper;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.StoreResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;

/**
 * Java REST Wrapper of the IPFS-Store module
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public class RestIPFSStoreWrapperImpl implements IPFSStoreWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestIPFSStoreWrapperImpl.class);

    private static final String BASE_API_PATH = "/ipfs-store";
    private static final String STORE_API_PATH = "/store";
    private static final String INDEX_API_PATH = "/index";
    private static final String FETCH_API_PATH = "/fetch";
    private static final String SEARCH_API_PATH = "/search";
    private static final String STORE_INDEX_API_PATH = "/store_index";
    private static final String CREATE_INDEX_API_PATH = "/config/index";
    private static final String DEFAULT_MIMETYPE = "application/octet-stream";
    private static final String MULTIPART_FILE = "file";
    private static final String MULTIPART_REQUEST = "request";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String endpoint;

    public RestIPFSStoreWrapperImpl(String endpoint) {
        this.endpoint = endpoint;

        this.restTemplate = new RestTemplate();

        this.mapper = new ObjectMapper();
        this.mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void createIndex(String indexName) throws IPFSStoreException {
        try {
            LOGGER.debug("createIndex [indexName={}]", indexName);

            restTemplate.postForLocation(
                    this.endpoint + BASE_API_PATH + CREATE_INDEX_API_PATH + "/" + indexName,
                    HttpEntity.EMPTY);

            LOGGER.debug("Index [indexName={}] created !", indexName);


        } catch (RestClientException ex) {
            LOGGER.error("Error while creating the index [index={}]", indexName, ex);
            throw new IPFSStoreException("Error while creating the index [index=" + indexName + "]", ex);
        }
    }

    public String store(byte[] file) throws IPFSStoreException {

        try {
            LOGGER.debug("store [size=" + file.length + "]");

            ByteArrayResource content = new ByteArrayResource(file) {
                @Override
                public String getFilename() {
                    return UUID.randomUUID().toString();
                }
            };

            MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

            // creating an HttpEntity for the binary part
            HttpHeaders contentHeader = new HttpHeaders();
            //contentHeader.setContentType(MediaType.valueOf(request.getContentType() == null ? DEFAULT_MIMETYPE : request.getContentType())); 
            HttpEntity<ByteArrayResource> contentHttpEntity = new HttpEntity<>(content, contentHeader);


            // putting the two parts in one request
            multipartRequest.add(MULTIPART_FILE, contentHttpEntity);

            // creating the final request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, headers);

            ResponseEntity<StoreResponse> response = restTemplate.exchange(
                    this.endpoint + BASE_API_PATH + STORE_API_PATH,
                    HttpMethod.POST,
                    requestEntity,
                    StoreResponse.class);

            LOGGER.debug("store [] : hash=" + response.getBody().getHash());

            return response.getBody().getHash();

        } catch (RestClientException ex) {
            LOGGER.error("Error while storing the file", ex);
            throw new IPFSStoreException("Error while storing the file", ex);
        }
    }

    public IndexerResponse index(IndexerRequest request) throws IPFSStoreException {

        try {
            LOGGER.debug("index [request={}]", request);

            HttpHeaders httpHeader = new HttpHeaders();
            httpHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IndexerRequest> httpRequest = new HttpEntity<>(request, httpHeader);

            IndexerResponse response = restTemplate.postForObject(
                    this.endpoint + BASE_API_PATH + INDEX_API_PATH,
                    httpRequest,
                    IndexerResponse.class);


            LOGGER.debug("index [request={}] : {}", request, response);

            return response;

        } catch (RestClientException ex) {
            LOGGER.error("Error while indexing the content", ex);
            throw new IPFSStoreException("Error while indexing the content", ex);
        }
    }

    public IndexerResponse storeAndIndex(byte[] file, IndexerRequest request) throws IPFSStoreException {

        try {
            LOGGER.debug("storeAndIndex [request={}]", request);

            ByteArrayResource content = new ByteArrayResource(file) {
                @Override
                public String getFilename() {
                    return UUID.randomUUID().toString();
                }
            };

            MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

            // creating an HttpEntity for the JSON part
            HttpHeaders requestHeader = new HttpHeaders();
            requestHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IndexerRequest> requestHttpEntity = new HttpEntity<>(request, requestHeader);

            // creating an HttpEntity for the binary part
            HttpHeaders contentHeader = new HttpHeaders();
            contentHeader.setContentType(MediaType.valueOf(request.getContentType() == null ? DEFAULT_MIMETYPE : request.getContentType()));
            HttpEntity<ByteArrayResource> contentHttpEntity = new HttpEntity<>(content, contentHeader);


            // putting the two parts in one request
            multipartRequest.add(MULTIPART_FILE, contentHttpEntity);
            multipartRequest.add(MULTIPART_REQUEST, requestHttpEntity);

            // creating the final request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, headers);

            ResponseEntity<IndexerResponse> response = restTemplate.exchange(
                    this.endpoint + BASE_API_PATH + STORE_INDEX_API_PATH,
                    HttpMethod.POST,
                    requestEntity,
                    IndexerResponse.class);

            LOGGER.debug("storeAndIndex [request={}] : {}", request, response.getBody());

            return response.getBody();

        } catch (RestClientException ex) {
            LOGGER.error("Error while storing and indexing the content request=" + request, ex);
            throw new IPFSStoreException("Error while storing and indexing the content request=" + request, ex);
        }
    }

    public byte[] fetch(String indexName, String hash) throws IPFSStoreException {

        try {
            LOGGER.debug("fetch [indexName={}, hash={}]", indexName, hash);

            restTemplate.getMessageConverters().add(
                    new ByteArrayHttpMessageConverter());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    this.endpoint + BASE_API_PATH + FETCH_API_PATH + "/" + indexName + "/" + hash,
                    HttpMethod.GET,
                    entity,
                    byte[].class);

            LOGGER.debug("fetch [indexName={}, hash={}] : {}", indexName, hash, response);

            return response.getBody();

        } catch (RestClientException ex) {
            LOGGER.error("Error while fetching the content", ex);
            throw new IPFSStoreException("Error while fetching the content", ex);
        }
    }

    public Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreException {

        try {
            LOGGER.debug("Search [indexName={}, query={}]", indexName, query);

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                    .fromUriString(this.endpoint + BASE_API_PATH + SEARCH_API_PATH)
                    .path("/" + indexName);

            if (query != null) {
                uriComponentsBuilder
                        .queryParam("query", mapper.writeValueAsString(query));
            }

            if (pageable != null) {
                uriComponentsBuilder
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize());
            }

            if (pageable != null && pageable.getSort() != null) {
                Order order = pageable.getSort().iterator().next();
                uriComponentsBuilder
                        .queryParam("sort", order.getProperty())
                        .queryParam("dir", order.isAscending() ? "ASC" : "DESC");
            }

            URI url = uriComponentsBuilder.build().encode().toUri();

            LOGGER.trace("url=" + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<RestResponsePage<Metadata>> response =
                    restTemplate.exchange(url,
                            HttpMethod.GET, entity, new ParameterizedTypeReference<RestResponsePage<Metadata>>() {
                            });

            LOGGER.trace("result" + response.getBody());

            LOGGER.debug("Search [indexName={}, query={}] : {} result(s)", indexName, query, response.getBody().getTotalElements());

            return response.getBody();

        } catch (RestClientException | JsonProcessingException ex) {
            LOGGER.error("Error while searching [indexName={}, query={}]", indexName, query, ex);
            throw new IPFSStoreException("Error while searching  [indexName=" + indexName + ", query=\"+query+\"]", ex);
        }
    }

    public RestTemplate getClient() {
        return restTemplate;
    }

}
