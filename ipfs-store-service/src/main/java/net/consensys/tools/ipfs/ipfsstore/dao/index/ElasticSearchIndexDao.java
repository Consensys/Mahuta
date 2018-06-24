package net.consensys.tools.ipfs.ipfsstore.dao.index;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.exception.TechnicalException;

/**
 * ElasticSearch implementation of IndexDao
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Slf4j
public class ElasticSearchIndexDao implements IndexDao {

    private static final String NULL = "null"; // must be lower case

    private static final String ERROR_NOT_NULL_OR_EMPTY = "cannot be null or empty";

    private final ObjectMapper mapper;

    @SuppressWarnings("unused") // Needed to keep the connection alive
    private final PreBuiltTransportClient preBuiltTransportClient;
    private final TransportClient client;
    private final boolean indexNullValues;

    /*
     * Constructor
     */
    public ElasticSearchIndexDao(PreBuiltTransportClient preBuiltTransportClient,
            TransportClient client, boolean indexNullValues) {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        this.indexNullValues = indexNullValues;
        this.preBuiltTransportClient = preBuiltTransportClient;
        this.client = client;
    }

    /**
     * Destroy
     */
    @PreDestroy
    public void destroy() {
        try {
            log.info("Closing ElasticSearch client");
            if (client != null) {
                client.close();
            }

        } catch (final Exception e) {
            log.error("Error closing ElasticSearch client: ", e);
        }
    }

    @Override
    public Result check() {
        log.debug("check elastic search health ...");

        try {
            // NEED ELASTIC LICENSES TO REQUEST CLUSTER_HEATLH
            // final ClusterHealthStatus status =
            // client.admin().cluster().prepareHealth().get().getStatus();
            // log.debug("status={}", status.toString());
            //
            // if (status == ClusterHealthStatus.RED) {
            // return Result.unhealthy("Last status: %s", status.name());
            // } else {
            // return Result.healthy("Last status: %s", status.name());
            // }

            client.admin().indices().getIndex(new GetIndexRequest()).actionGet().getIndices();

            log.debug("check elastic search health : OK");

            return Result.healthy("ElasticSearch is OK");

        } catch (Exception e) {
            log.error("Error whilst checking ElasticSearch health", e);
            return Result.unhealthy(e);
        }
    }

    @Override
    public String index(String index, String documentId, String hash, String contentType,
            List<IndexField> indexFields) {
        log.debug("Index document in ElasticSearch [index: {}, documentId:{}, indexFields: {}]",
                index, documentId, indexFields);

        // Validation
        if (StringUtils.isEmpty(index))
            throw new IllegalArgumentException("index " + ERROR_NOT_NULL_OR_EMPTY);
        if (StringUtils.isEmpty(hash))
            throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            DocWriteResponse response;
            Map<String, Object> source = new HashMap<>();

            // Populate the ElasticSearch Document
            source.put(IndexDao.HASH_INDEX_KEY, hash);
            source.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
            if (indexFields != null) {
                source.putAll(convert(indexFields));
            }

            log.debug(source.toString());

            if (!this.doesExist(index, documentId)) {
                response = client.prepareIndex(index.toLowerCase(), index.toLowerCase(), documentId)
                        .setSource(convertObjectToJsonString(source), XContentType.JSON).get();

            } else {
                response = client
                        .prepareUpdate(index.toLowerCase(), index.toLowerCase(), documentId)
                        .setDoc(convertObjectToJsonString(source), XContentType.JSON).get();
            }

            log.debug(
                    "Document indexed ElasticSearch [index: {}, documentId:{}, indexFields: {}]. Result ID= {} ",
                    index, documentId, indexFields, response.getId());

            this.refreshIndex(index);

            return response.getId();

        } catch (Exception ex) {
            log.error(
                    "Error while indexing document into ElasticSearch [index: {}, documentId:{}, indexFields: {}]",
                    index, documentId, indexFields, ex);
            throw new TechnicalException(
                    "Error while indexing document into ElasticSearch: " + ex.getMessage());
        }
    }

    @Override
    public Metadata searchById(Optional<String> index, String id) throws NotFoundException {
        log.debug("Search in ElasticSearch by ID [index: {}, ID: {}]", index, id);

        // Validation
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("id" + ERROR_NOT_NULL_OR_EMPTY);

        try {
            String indexFormatted = formatIndex(index);

            GetResponse response = client.prepareGet(indexFormatted, indexFormatted, id).get();

            log.trace("Search one document in ElasticSearch [index: {}, ID: {}] : response= {}",
                    indexFormatted, id, response);

            if (!response.isExists()) {
                throw new NotFoundException(
                        "Document [index: " + indexFormatted + ", ID: " + id + "] not found");
            }

            Metadata metadata = convert(response.getIndex(), response.getId(),
                    response.getSourceAsMap());

            log.debug("Search in ElasticSearch by ID [index: {}, ID: {}]: {}", index, id, metadata);

            return metadata;

        } catch (NotFoundException ex) {
            log.warn("Error while searching into ElasticSearch [index: {}, ID: {}]", index, id, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error while searching into ElasticSearch [index: {}, ID: {}]", index, id,
                    ex);
            throw new TechnicalException(
                    "Error while searching into ElasticSearch: " + ex.getMessage());
        }
    }

    @Override
    public List<Metadata> search(Pageable pageable, Optional<String> index, Query query) {
        log.debug("Search documents in ElasticSearch [index: {}, query: {}]", index, query);

        // Validation
        if (pageable == null)
            throw new IllegalArgumentException("pageable " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            String indexFormatted = formatIndex(index);

            SearchRequestBuilder requestBuilder = client.prepareSearch(indexFormatted)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(convertQuery(query))
                    .setFrom(pageable.getOffset()).setSize(pageable.getPageSize());

            if (pageable.getSort() != null) {
                for (Order order : pageable.getSort()) {
                    requestBuilder.addSort(new FieldSortBuilder(order.getProperty())
                            .order(order.isAscending() ? SortOrder.ASC : SortOrder.DESC)
                            .unmappedType("date"));
                }
            }

            log.trace(requestBuilder.toString());

            SearchResponse searchResponse = requestBuilder.execute().actionGet();

            log.trace("Search documents in ElasticSearch [index: {}, query: {}]: {}", index, query,
                    searchResponse);

            List<Metadata> result = Arrays.stream(searchResponse.getHits().getHits())
                    .map(hit -> convert(hit.getIndex(), hit.getId(), hit.getSourceAsMap()))
                    .collect(Collectors.toList());

            log.debug("Search documents in ElasticSearch [index: {}, query: {}]: {}", index, query,
                    result);

            return result;

        } catch (Exception ex) {
            log.error("Error while searching documents into ElasticSearch [index: {}, query: {}]",
                    index, query, ex);
            throw new TechnicalException(
                    "Error while searching documents into ElasticSearch: " + ex.getMessage());
        }
    }

    @Override
    public long count(Optional<String> index, Query query) {
        log.debug("Count in ElasticSearch [index: {}, query: {}]", index, query);

        try {
            String indexFormatted = formatIndex(index);

            SearchResponse countResponse = client.prepareSearch(indexFormatted)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(convertQuery(query))
                    .setSize(0).get();

            log.trace("Count in ElasticSearch [index: {}, query: {}]: {}", index, query,
                    countResponse);

            return countResponse.getHits().getTotalHits();

        } catch (Exception ex) {
            log.error("Error while counting into ElasticSearch [index: {}, query: {}]", index,
                    query, ex);
            throw new TechnicalException(
                    "Error while counting into ElasticSearch: " + ex.getMessage());
        }
    }

    @Override
    public void createIndex(String index) {
        log.debug("Create index in ElasticSearch [index: {}]", index);

        // Validation
        if (StringUtils.isEmpty(index))
            throw new IllegalArgumentException("index " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            boolean exists = client.admin().indices().prepareExists(index).execute().actionGet()
                    .isExists();

            if (!exists) {
                client.admin().indices().prepareCreate(index).get();
                log.debug("Index [index: {}] created in ElasticSearch", index);

            } else {
                log.debug("Index [index: {}] already exists in ElasticSearch", index);
            }

        } catch (Exception ex) {
            log.error("Error while creating the index [index: {}] into ElasticSearch", index, ex);
            throw new TechnicalException(
                    "Error while creating the index into ElasticSearch: " + ex.getMessage());
        }
    }

    /**
     * Check if a document exists in E.S.
     *
     * @param index
     *            Index Name
     * @param id
     *            Document ID
     * @return true/false
     * @throws ElasticsearchException
     */
    private Boolean doesExist(String index, String id) {
        if (StringUtils.isEmpty(id)) {
            return false;
        }

        GetResponse response = client.prepareGet(index, index, id).setRefresh(true).get();
        return response.isExists();
    }

    /**
     * Convert a list of IndexField (key/value) to a Map
     *
     * @param indexFields
     *            List of IndexField
     * @return Map
     */
    private Map<String, Object> convert(List<IndexField> indexFields) {
        if (indexFields == null) {
            return null;
        }

        return indexFields.stream().collect(Collectors.toMap(field -> field.getName(),
                field -> handleNullValue(field.getValue())));
    }

    /**
     * Replace null or empty string value by NULL to add it in the index (E.S. doesn't index null
     * value)
     *
     * @param value
     *            Value
     * @return Value replaced by NULL if null or empty
     */
    private Object handleNullValue(Object value) {
        if (indexNullValues
                && (value == null || (value instanceof String && ((String) value).length() == 0))) {
            return NULL;
        } else {
            return value;
        }
    }

    /**
     * Convert a ElasticSearch result to a Metadata
     *
     * @param index
     *            Index
     * @param id
     *            ID
     * @param sourceMap
     *            Map of attributes
     * @return Metadata
     */
    private static Metadata convert(String index, String id, Map<String, Object> sourceMap) {
        String hash = null;
        String contentType = null;

        if (sourceMap != null) {

            // Extract special key __hash
            if (sourceMap.containsKey(HASH_INDEX_KEY) && sourceMap.get(HASH_INDEX_KEY) != null) {
                hash = sourceMap.get(HASH_INDEX_KEY).toString();
            }
            // Extract special key __content_type
            if (sourceMap.containsKey(CONTENT_TYPE_INDEX_KEY)
                    && sourceMap.get(CONTENT_TYPE_INDEX_KEY) != null) {
                contentType = sourceMap.get(CONTENT_TYPE_INDEX_KEY).toString();
            }
        }

        return new Metadata(index, id, hash, contentType, convert(sourceMap));
    }

    /**
     * Convert a Map to a list of IndexField (key/value)
     *
     * @param indexFields
     *            Map
     * @return List of IndexField
     */
    private static List<IndexField> convert(Map<String, Object> indexFields) {
        if (indexFields == null) {
            return Collections.emptyList();
        }

        return indexFields.entrySet().stream()
                .map(field -> new IndexField(field.getKey(), field.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Convert a IPFS-Store Query to a ElasticSearch query
     *
     * @param query
     *            IPFS-Store Query
     * @return ElasticSearch query
     */
    private QueryBuilder convertQuery(Query query) {
        log.trace("Converting query: " + query);

        BoolQueryBuilder elasticSearchQuery = QueryBuilders.boolQuery();

        if (query == null || query.isEmpty()) {
            return QueryBuilders.matchAllQuery();
        }

        query.getFilterClauses().forEach(f -> {

            Object value = handleNullValue(f.getValue());

            try {

                switch (f.getOperation()) {
                case full_text:
                    elasticSearchQuery.must(QueryBuilders.multiMatchQuery(value, f.getNames())
                            .type(Type.PHRASE_PREFIX));
                    break;
                case equals:
                    elasticSearchQuery.must(QueryBuilders.termQuery(f.getName(), value));
                    break;
                case not_equals:
                    elasticSearchQuery.mustNot(QueryBuilders.termQuery(f.getName(), value));
                    break;
                case contains:
                    elasticSearchQuery.must(QueryBuilders.matchQuery(f.getName(), value));
                    break;
                case in:
                    elasticSearchQuery.filter(QueryBuilders.termsQuery(f.getName(),
                            asList((Object[]) value).stream().map(o -> o.toString().toLowerCase())
                                    .collect(Collectors.toList())));
                    break;
                case lt:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).lt(value));
                    break;
                case lte:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).lte(value));
                    break;
                case gt:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).gt(value));
                    break;
                case gte:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).gte(value));
                    break;
                default:
                    log.warn("Operation [" + f.getOperation() + "] not supported for  filter [" + f
                            + "]- Ignore it!");
                    break;
                }

            } catch (Exception e) {
                log.warn("Error while converting filter [" + f + "] - Ignore it!", e);
            }
        });

        log.debug(elasticSearchQuery.toString());

        return elasticSearchQuery;
    }

    /**
     * Convert an object to a JSON String
     *
     * @param object
     *            Object to convert to a JSON
     * @return JSON representation of the object
     */
    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("Exception occur:{}", ex);
            return null;
        }
    }

    /**
     * Refresh an index
     *
     * @param index
     *            Index name
     */
    private void refreshIndex(String index) {
        this.client.admin().indices().prepareRefresh(index).get();
    }

    /**
     * Format the ElasticSearch index name (lowercase) if null, the wildcard "_all" is used
     * 
     * @param index
     *            Optional index name
     * @return indice
     */
    private static String formatIndex(Optional<String> index) {
        return index.map(String::toLowerCase).orElse("_all");
    }

}
