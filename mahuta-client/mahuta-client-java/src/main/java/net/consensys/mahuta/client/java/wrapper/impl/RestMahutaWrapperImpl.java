package net.consensys.mahuta.client.java.wrapper.impl;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.client.java.utils.RestResponsePage;
import net.consensys.mahuta.client.java.wrapper.MahutaWrapper;
import net.consensys.mahuta.dto.IndexerRequest;
import net.consensys.mahuta.dto.IndexerResponse;
import net.consensys.mahuta.dto.Metadata;
import net.consensys.mahuta.dto.StoreResponse;
import net.consensys.mahuta.dto.query.Query;
import net.consensys.mahuta.exception.MahutaException;
import net.consensys.mahuta.exception.NotFoundException;
import net.consensys.mahuta.exception.TechnicalException;
import net.consensys.mahuta.exception.TimeoutException;
import net.consensys.mahuta.exception.ValidationException;

/**
 * Java REST Wrapper of the Mahuta module
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Slf4j
public class RestMahutaWrapperImpl implements MahutaWrapper {

    private static final String BASE_API_PATH = "/mahuta";
    private static final String STORE_API_PATH = "/raw/store";
    private static final String INDEX_API_PATH = "/raw/index";
    private static final String FETCH_API_PATH = "/query/fetch";
    private static final String SEARCH_API_PATH = "/query/search";
    private static final String STORE_INDEX_API_PATH = "/raw/store_index";
    private static final String CREATE_INDEX_API_PATH = "/config/index";
    private static final String REMOVE_BY_ID_API_PATH = "/delete/id";
    private static final String REMOVE_BY_HASH_API_PATH = "/delete/hash";
    private static final String DEFAULT_MIMETYPE = "application/octet-stream";
    private static final String MULTIPART_FILE = "file";
    private static final String MULTIPART_REQUEST = "request";

    private RestTemplate restTemplate;
    
    private final ObjectMapper mapper;

    private final String endpoint;

    public RestMahutaWrapperImpl(String endpoint) {
        this.endpoint = endpoint;

        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(
                new ByteArrayHttpMessageConverter());

        this.mapper = new ObjectMapper();
        this.mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private MahutaException handleHTTPExceptiion(HttpClientErrorException e) {
        HttpStatus status = e.getStatusCode();
        if (status == HttpStatus.NOT_FOUND) { 
            return new NotFoundException(e.getResponseBodyAsString()); 
        } else  if (status == HttpStatus.BAD_REQUEST) { 
            return new ValidationException(e.getResponseBodyAsString()); 
        } else  if (status == HttpStatus.REQUEST_TIMEOUT) { 
            return new TimeoutException(e.getResponseBodyAsString());
        }  else {
            throw new TechnicalException(e.getResponseBodyAsString());
        }
    }
    
    public void createIndex(String index) throws MahutaException {
        try {
            log.debug("createIndex [indexName={}]", index);

            restTemplate.postForLocation(
                    this.endpoint + BASE_API_PATH + CREATE_INDEX_API_PATH + "/" + index,
                    HttpEntity.EMPTY);

            log.debug("Index [indexName={}] created !", index);


        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
    }

    public String store(byte[] file) throws MahutaException {

        try {
            log.debug("store [size={}]", file.length);

            ByteArrayResource content = new ByteArrayResource(file) {
                @Override
                public String getFilename() {
                    return UUID.randomUUID().toString();
                }
            };

            MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

            // creating an HttpEntity for the binary part
            HttpHeaders contentHeader = new HttpHeaders();
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

            log.debug("store [] : hash= {}", response.getBody().getHash());

            return response.getBody().getHash();

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
    }

    public IndexerResponse index(IndexerRequest request) throws MahutaException {

        try {
            log.debug("index [request={}]", request);

            HttpHeaders httpHeader = new HttpHeaders();
            httpHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IndexerRequest> httpRequest = new HttpEntity<>(request, httpHeader);

            IndexerResponse response = restTemplate.postForObject(
                    this.endpoint + BASE_API_PATH + INDEX_API_PATH,
                    httpRequest,
                    IndexerResponse.class);


            log.debug("index [request={}] : {}", request, response);

            return response;

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
    }

    public IndexerResponse storeAndIndex(byte[] file, IndexerRequest request) throws MahutaException {

        try {
            log.debug("storeAndIndex [request={}]", request);

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

            log.debug("storeAndIndex [request={}] : {}", request, response.getBody());

            return response.getBody();

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
    }

    public byte[] fetch(String index, String hash) throws MahutaException {

        try {
            log.debug("fetch [indexName={}, hash={}]", index, hash);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    this.endpoint + BASE_API_PATH + FETCH_API_PATH + "/" + hash + "?index=" + index,
                    HttpMethod.GET,
                    entity,
                    byte[].class);

            log.debug("fetch [indexName={}, hash={}] : {}", index, hash, response);

            return response.getBody();

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
    }

    public Page<Metadata> search(String index, Query query, Pageable pageable) throws MahutaException {

        try {
            log.debug("Search [indexName={}, query={}]", index, query);

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                    .fromUriString(this.endpoint + BASE_API_PATH + SEARCH_API_PATH + "?index=" + index);

            if (query == null) {
                query = Query.newQuery();
            }
            
            if (pageable != null) {
                uriComponentsBuilder
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize());
            }

            if (pageable != null && pageable.getSort() != null && pageable.getSort().isSorted()) {
                Order order = pageable.getSort().iterator().next();
                uriComponentsBuilder
                        .queryParam("sort", order.getProperty())
                        .queryParam("dir", order.isAscending() ? "ASC" : "DESC");
            }

            URI url = uriComponentsBuilder.build().encode().toUri();

            log.trace("url={}", url);
            log.trace("query={}", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<Query> entity = new HttpEntity<>(query, headers);

            ResponseEntity<RestResponsePage<Metadata>> response =
                    restTemplate.exchange(url,
                            HttpMethod.POST, entity, new ParameterizedTypeReference<RestResponsePage<Metadata>>() {
                            });

            log.trace("result {}", response.getBody());

            log.debug("Search [indexName={}, query={}] : {} result(s)", index, query, response.getBody().getTotalElements());

            return response.getBody();

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
    }
    

	public void removeById(String indexName, String id) throws MahutaException {
		
        try {
            log.debug("removeById [indexName={}, id={}]", indexName, id);

            HttpHeaders httpHeader = new HttpHeaders();
            httpHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IndexerRequest> httpRequest = new HttpEntity<>(httpHeader);

            restTemplate.delete(
                    this.endpoint + BASE_API_PATH + REMOVE_BY_ID_API_PATH + "/" + id +  "?index=" + indexName,
                    httpRequest);


            log.debug("removeById [indexName={}, id={}] DONE", indexName, id);

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
	}

	public void removeByHash(String indexName, String hash) throws MahutaException {

        try {
            log.debug("removeByHash [indexName={}, hash={}]", indexName, hash);

            HttpHeaders httpHeader = new HttpHeaders();
            httpHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IndexerRequest> httpRequest = new HttpEntity<>(httpHeader);

            restTemplate.delete(
                    this.endpoint + BASE_API_PATH + REMOVE_BY_HASH_API_PATH + "/" + hash +  "?index=" + indexName,
                    httpRequest);


            log.debug("removeByHash [indexName={}, id={}] DONE", indexName, hash);

        } catch (HttpClientErrorException ex) {
            throw handleHTTPExceptiion(ex);
        }
	}

    public RestTemplate getClient() {
        return restTemplate;
    }

}
