package net.consensys.mahuta.core.indexer.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.NoIndexException;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Slf4j
public class ElasticSearchService implements IndexingService {

    private static final String DEFAULT_TYPE = "_doc";
    private static final String ALL_INDICES = "_all";
    private static final String NULL = "null";
    private static final Integer RETRY_ON_CONFLICT = 5;

    private final ElasticSearchSettings settings;
    private final TransportClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private ElasticSearchService(ElasticSearchSettings settings, TransportClient transportClient) {
        ValidatorUtils.rejectIfNull("settings", settings);
        ValidatorUtils.rejectIfNull("transportClient", transportClient);

        this.settings = settings;
        this.client = transportClient;
    }

    public static ElasticSearchService connect(String clusterName) {
        ValidatorUtils.rejectIfEmpty("clusterName", clusterName);
        return connect(ElasticSearchSettings.DEFAULT_HOST, ElasticSearchSettings.DEFAULT_PORT, clusterName);
    }

    public static ElasticSearchService connect(String host, Integer port, String clusterName) {
        ElasticSearchSettings settings = ElasticSearchSettings.of(host, port, clusterName);

        try {
            // WARNING pbtc is never closed -> Close the transportClient as well...
            PreBuiltTransportClient pbtc = new PreBuiltTransportClient(
                    Settings.builder().put("cluster.name", clusterName).build());
            TransportClient transportClient = pbtc
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));

            log.info("Connected to ElasticSearch [host: {}, port: {}, cluster: {}] : {}", host, port, clusterName,
                    transportClient.listedNodes().toString());

            return new ElasticSearchService(settings, transportClient);

        } catch (UnknownHostException ex) {
            log.error("Error while connecting to ElasticSearch [host: {}, port: {}, cluster: {}]", host, port,
                    clusterName, ex);
            throw new ConnectionException("Error whilst connecting to ElasticSearch", ex);
        }
    }

    public ElasticSearchService configureIndexNullValue(boolean indexNullValue) {
        this.settings.setIndexNullValue(indexNullValue);
        return this;
    }

    public ElasticSearchService withIndex(String indexName) {
        return this.withIndex(indexName, null);
    }

    @Override
    public void createIndex(String indexName) {
        this.createIndex(indexName, null);
    }

    public ElasticSearchService withIndex(String indexName, InputStream configuration) {
        ValidatorUtils.rejectIfEmpty("indexName", indexName);

        this.createIndex(indexName, configuration);

        return this;
    }

    @Override
    public void createIndex(String indexName, InputStream configuration) {
        log.debug("Create index in ElasticSearch [indexName: {}, configuration present: {}]", indexName,
                configuration != null);

        // Validation
        ValidatorUtils.rejectIfEmpty("indexName", indexName);

        try {
            // Format index
            indexName = indexName.toLowerCase();

            // Check existence
            boolean exists = indexExists(indexName);

            if (!exists) {
                CreateIndexRequestBuilder request = client.admin()
                        .indices()
                        .prepareCreate(indexName);
                
                if (configuration != null) {
                    request.setSource(IOUtils.toString(configuration, StandardCharsets.UTF_8), XContentType.JSON);
                } else {
                    request.addMapping(DEFAULT_TYPE, HASH_INDEX_KEY, "type=keyword", CONTENT_TYPE_INDEX_KEY, "type=keyword");
                }
                
                request.get();

                log.debug("Index [indexName: {}] created in ElasticSearch", indexName);

            } else {
                log.debug("Index [indexName: {}] already exists in ElasticSearch", indexName);
            }

        } catch (IOException ex) {
            log.error("Error whist reading configuration InputStream", ex);
            throw new TechnicalException("Error whist reading configuration InputStream", ex);
        }
    }
    


    @Override
    public List<String> getIndexes() {
        
        log.debug("Get indices in ElasticSearch []");

        String[] response = client.admin().indices().prepareGetIndex().setFeatures().get().getIndices();
        
        log.debug("Get indices in ElasticSearch []: {}", Arrays.asList(response));

        return Arrays.asList(response);
    }

    @Override
    public String index(String indexName, String indexDocId, String contentId, String contentType,
            Map<String, Object> indexFields) {
        return index(indexName, indexDocId, contentId, contentType, null, indexFields);
    }

    @Override
    public String index(String indexName, String indexDocId, String contentId, String contentType, byte[] content,
            Map<String, Object> indexFields) {

        log.debug(
                "Index document in ElasticSearch [indexName: {}, indexDocId:{}, contentId: {}, contentType: {}, content: {}, indexFields: {}]",
                indexName, indexDocId, contentId, contentType, content!=null ? "present": "not present", indexFields);

        // Validation
        ValidatorUtils.rejectIfEmpty("indexName", indexName);

        // Format index
        indexName = indexName.toLowerCase();

        // Check if index exists
        if(!indexExists(indexName)) {
            throw new NoIndexException(indexName);
        }
        
        // Populate the ElasticSearch Document
        Map<String, Object> source = new HashMap<>();
        source.put(HASH_INDEX_KEY, contentId);
        source.put(CONTENT_TYPE_INDEX_KEY, contentType);
        source.put(PINNED_KEY, false);
        Optional.ofNullable(content)
            .map(bytearray -> Base64.getEncoder().encode(bytearray))
            .ifPresent(base64 -> source.put(CONTENT_INDEX_KEY, new String(base64)));
        
        if (indexFields != null) {
            source.putAll(transformFields(indexFields));
        }
        log.trace("source={}", source.toString());

        // Upsert
        DocWriteResponse response;
        if (indexDocId == null || !this.documentExists(indexName, indexDocId)) {
            response = client.prepareIndex(indexName, DEFAULT_TYPE, indexDocId)
                    .setSource(convertObjectToJsonString(source), XContentType.JSON).get();

        } else {
            response = client.prepareUpdate(indexName, DEFAULT_TYPE, indexDocId)
                    .setRetryOnConflict(RETRY_ON_CONFLICT)
                    .setDoc(convertObjectToJsonString(source), XContentType.JSON).get();
        }

        log.debug(
                "Document indexed ElasticSearch [indexName: {}, indexDocId:{}, contentId: {}, contentType: {}, indexFields: {}]. Result ID= {} ",
                indexName, indexDocId, contentId, contentType, indexFields, response.getId());

        this.refreshIndex(indexName);

        return response.getId();
    }

    @Override
    public void updateField(String indexName, String indexDocId, String key, Object value) {
        log.debug("Update field on document in ElasticSearch [indexName: {}, indexDocId: {}, key: {}, value: {}]", indexName, indexDocId, key, value);

        // Validation
        ValidatorUtils.rejectIfEmpty("indexName", indexName);
        ValidatorUtils.rejectIfEmpty("indexDocId", indexDocId);
        ValidatorUtils.rejectIfEmpty("key", key);

        // Format index
        indexName = indexName.toLowerCase();
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("value", value);
            
            client.prepareUpdate(indexName, DEFAULT_TYPE, indexDocId)
                .setRetryOnConflict(RETRY_ON_CONFLICT)
                .setScript(new Script(
                        ScriptType.INLINE,
                        "painless",
                        "ctx._source."+key+" = params.value",
                        params
                )).execute().actionGet();

            log.debug("Field updated on document in ElasticSearch [indexName: {}, indexDocId: {}, key: {}, value: {}]", indexName, indexDocId, key, value);

            this.refreshIndex(indexName);

        } catch (Exception ex) {
            log.error("Error while updating field [indexName: {}, indexDocId: {}, key: {}, value: {}]", indexName, indexDocId, key, value, ex);
            throw new TechnicalException("Error while updating key " + key + " of doc indexDocId: " + indexDocId, ex);
        }
    }

    @Override
    public void deindex(String indexName, String indexDocId) {

        log.debug("Deindex document in ElasticSearch [indexName: {}, indexDocId:{}]", indexName, indexDocId);

        // Validation
        ValidatorUtils.rejectIfEmpty("indexName", indexName);
        ValidatorUtils.rejectIfEmpty("indexDocId", indexDocId);

        // Format index
        indexName = indexName.toLowerCase();

        // Check if index exists
        if(!indexExists(indexName)) {
            throw new NoIndexException(indexName);
        }
        
        if (!this.documentExists(indexName, indexDocId)) {
            throw new NotFoundException("Document [indexName: " + indexName + ", id: " + indexDocId + "] not found");
        }

        client.prepareDelete(indexName, DEFAULT_TYPE, indexDocId).get();

        log.debug("Document deindexed ElasticSearch [indexName: {}, indexDocId:{}]", indexName, indexDocId);

        this.refreshIndex(indexName);
    }

    @Override
    public Metadata getDocument(String indexName, String indexDocId) {
        log.debug("Get document in ElasticSearch [indexName: {}, indexDocId:{}]", indexName, indexDocId);

        // Validation
        ValidatorUtils.rejectIfEmpty("indexName", indexName);
        ValidatorUtils.rejectIfEmpty("indexDocId", indexDocId);

        // Format index
        indexName = indexName.toLowerCase();

        GetResponse response = client.prepareGet(indexName, DEFAULT_TYPE, indexDocId).get();

        log.trace("Get document in ElasticSearch [indexName: {}, indexDocId: {}] : response= {}", indexName, indexDocId,
                response);

        if (!response.isExists()) {
            throw new NotFoundException(
                    "Document [indexName: " + indexName + ", indexDocId: " + indexDocId + "] not found");
        }

        return convert(response.getIndex(), response.getId(), response.getSource());
    }

    @Override
    public Page<Metadata> searchDocuments(String indexName, Query query, PageRequest pageRequest) {

        log.debug("Search documents in ElasticSearch [indexName: {}, query: {}]", indexName, query);

        // Validation
        ValidatorUtils.rejectIfNull("pageRequest", pageRequest);

        // Format index
        indexName = Optional.ofNullable(indexName).map(String::toLowerCase).orElse(ALL_INDICES);

        // Build query
        SearchRequestBuilder requestBuilder = client.prepareSearch(indexName)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(buildQuery(query))
                .setFrom(pageRequest.getSize() * pageRequest.getPage()).setSize(pageRequest.getSize());

        if (pageRequest.getSort() != null) {
            requestBuilder.addSort(new FieldSortBuilder(pageRequest.getSort())
                    .order(pageRequest.isAscending() ? SortOrder.ASC : SortOrder.DESC).unmappedType("date"));
        }

        log.trace(requestBuilder.toString());

        // Run query
        SearchResponse searchResponse = requestBuilder.execute().actionGet();

        log.trace("Search documents in ElasticSearch [indexName: {}, query: {}]: {}", indexName, query, searchResponse);

        List<Metadata> result = Arrays.stream(searchResponse.getHits().getHits())
                .map(hit -> convert(hit.getIndex(), hit.getId(), hit.getSourceAsMap())).collect(Collectors.toList());

        log.debug("Search documents in ElasticSearch [indexName: {}, query: {}]: {}", indexName, query, result);

        return Page.of(pageRequest, result, Math.toIntExact(searchResponse.getHits().getTotalHits()));
    }

    private Map<String, Object> transformFields(Map<String, Object> indexFields) {

        return indexFields.entrySet().stream()
                .filter(e -> settings.isIndexNullValue() || e.getValue() != null)
                .collect(Collectors.toMap(Entry::getKey, e -> transformValue(e.getValue())));
    }

    private Object transformValue(Object value) {
        
        // Manage null values
        if (settings.isIndexNullValue()
                && (value == null || value instanceof String && ValidatorUtils.isEmpty((String) value))) {
            value = NULL;
        }
        
        // Convert Date to Timestamp
        if(value instanceof Date) {
            value = ((Date) value).getTime();
        }

        return value;
    }

    private Boolean documentExists(String indexName, String id) {
        GetResponse response = client.prepareGet(indexName, DEFAULT_TYPE, id).setRefresh(true).get();
        return response.isExists();
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("Exception occur:{}", ex);
            throw new TechnicalException("Error while convering object to JSON", ex);
        }
    }

    private void refreshIndex(String indexName) {
        this.client.admin().indices().prepareRefresh(indexName).get();
    }
    
    private boolean indexExists(String indexName) {
        return client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
    }

    private Metadata convert(String indexName, String documentId, Map<String, Object> sourceMap) {
        String contentId = null;
        String contentType = null;
        byte[] content = null;
        boolean pinned = false;

        if (sourceMap != null) {
            
            // Cast from mapping
            Map<String, String> mapping = this.getMapping(indexName);
            log.debug("mapping={}", mapping);
            sourceMap.forEach((key, value) -> {
                if (mapping.containsKey(key)) {
                    if ("date".equals(mapping.get(key))) {
                        Long date = Longs.tryParse(value.toString());
                        if (date != null) {
                            sourceMap.put(key, new Date(date));
                        }
                    }
                }

            });
            
            // Extract special key __hash
            if (sourceMap.containsKey(HASH_INDEX_KEY) && sourceMap.get(HASH_INDEX_KEY) != null) {
                contentId = sourceMap.get(HASH_INDEX_KEY).toString();
                sourceMap.remove(HASH_INDEX_KEY);
            }
            // Extract special key __content_type
            if (sourceMap.containsKey(CONTENT_TYPE_INDEX_KEY) && sourceMap.get(CONTENT_TYPE_INDEX_KEY) != null) {
                contentType = sourceMap.get(CONTENT_TYPE_INDEX_KEY).toString();
                sourceMap.remove(CONTENT_TYPE_INDEX_KEY);
            }
            // Extract special key __content
            if (sourceMap.containsKey(CONTENT_INDEX_KEY) && sourceMap.get(CONTENT_INDEX_KEY) != null) {
                content = Base64.getDecoder().decode(sourceMap.get(CONTENT_INDEX_KEY).toString());
                sourceMap.remove(CONTENT_INDEX_KEY);
            }
            // Extract special key __pinned
            if (sourceMap.containsKey(PINNED_KEY) && sourceMap.get(PINNED_KEY) != null) {
                pinned = (boolean) sourceMap.get(PINNED_KEY);
                sourceMap.remove(PINNED_KEY);
            }
        }
        
        
        return Metadata.of(indexName, documentId, contentId, contentType, content, pinned, sourceMap);
    }

    private QueryBuilder buildQuery(Query query) {
        log.trace("Converting query: " + query);

        if (query == null || query.isEmpty()) {
            return QueryBuilders.matchAllQuery();
        }

        // Initialize query
        BoolQueryBuilder elasticSearchQuery = QueryBuilders.boolQuery();

        // Filters
        query.getFilterClauses().forEach(f -> {

            Object value = transformValue(f.getValue());

            try {
                switch (f.getOperation()) {
                case FULL_TEXT:
                    elasticSearchQuery.must(QueryBuilders.multiMatchQuery(value, f.getNames()).type(Type.PHRASE_PREFIX));
                    break;
                case EQUALS:
                    elasticSearchQuery.must(QueryBuilders.termQuery(f.getName(), value));
                    break;
                case NOT_EQUALS:
                    elasticSearchQuery.mustNot(QueryBuilders.termQuery(f.getName(), value));
                    break;
                case CONTAINS:
                    elasticSearchQuery.must(QueryBuilders.matchQuery(f.getName(), value));
                    break;
                case IN:
                    if (value instanceof Collection<?>) {
                        Collection<?> values = (Collection<?>) value;
                        Collection<String> terms = values.stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        elasticSearchQuery.filter(QueryBuilders.termsQuery(f.getName(), terms));
                    } else {
                        throw new IllegalArgumentException("in operation: expected type Collection<?>");
                    }
                    break;
                case LT:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).lt(value));
                    break;
                case LTE:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).lte(value));
                    break;
                case GT:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).gt(value));
                    break;
                case GTE:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).gte(value));
                    break;
                default:
                    log.warn("Operation [" + f.getOperation() + "] not supported for  filter [" + f + "]- Ignore it!");
                    break;
                }

            } catch (Exception e) {
                log.warn("Error while converting filter [" + f + "] - Ignore it!", e);
            }
        });
        
        // Sub queries (OR)
        query.getSubFilterClauses()
            .forEach(q -> elasticSearchQuery.should(this.buildQuery(q)));

        
        log.debug(elasticSearchQuery.toString());

        return elasticSearchQuery;
    }
    
    private Map<String, String> getMapping(String indexName) {
        Map<String, String> mapping = new HashMap<>();

        GetMappingsResponse response = this.client.admin().indices().prepareGetMappings(indexName).get();
        log.debug("GetMappingsResponse={}", response);
       
        MappingMetaData mappingMetaData = response.getMappings().get(indexName).get(DEFAULT_TYPE).get();
        LinkedHashMap<String, LinkedHashMap> map = (LinkedHashMap<String, LinkedHashMap>) mappingMetaData.getSourceAsMap().get("properties");
        
        map.forEach((key, value) -> mapping.put(key, (String) value.get("type")));
        
        return mapping;
    }
}
