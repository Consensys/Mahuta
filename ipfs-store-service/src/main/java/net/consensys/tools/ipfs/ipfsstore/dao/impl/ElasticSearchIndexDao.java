package net.consensys.tools.ipfs.ipfsstore.dao.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.utils.Strings;

/**
 * ElasticSearch implementation of IndexDao
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
@Service
public class ElasticSearchIndexDao implements IndexDao {

    private static final Logger LOGGER = Logger.getLogger(IPFSStorageDao.class);
    
    private TransportClient client;
    
    /*
     * Constructor
     */
    @Autowired
    public ElasticSearchIndexDao(TransportClient client) {
        this.client = client;
    }
     

    
    @Override
    public String index(String indexName, String documentId, String hash, String contentType, List<IndexField> indexFields) throws DaoException {
        LOGGER.debug("Index document in ElasticSearch [indexName="+indexName+", documentId="+documentId+", indexFields="+indexFields+"] ...");
        
        // Validation
        if(Strings.isEmpty(indexName)) throw new IllegalArgumentException("indexName cannot be null or empty");
        if(Strings.isEmpty(hash)) throw new IllegalArgumentException("hash cannot be null or empty");
        
        try {
            DocWriteResponse response = null;
            Map<String, Object> source = new HashMap<>();
            
            // Populate the ElasticSearch Document
            source.put(IndexDao.HASH_INDEX_KEY, hash);
            source.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
            if(indexFields != null) {
                source.putAll(convert(indexFields)); 
            }
            
            if(!this.doesExist(indexName, documentId)) {
                response = client.prepareIndex(indexName.toLowerCase(), indexName.toLowerCase(), documentId)
                    .setSource(source)
                    .get();
                
            } else {
                response = client.prepareUpdate(indexName.toLowerCase(), indexName.toLowerCase(), documentId)
                    .setDoc(source)
                    .get();               
            }
            
            LOGGER.debug("Document indexed ElasticSearch [indexName="+indexName+", documentId="+documentId+", indexFields="+indexFields+"]. Result ID=" + response.getId());
            
            return response.getId();
            
        } catch(Exception ex) {
            LOGGER.debug("Error while indexing document into ElasticSearch [indexName="+indexName+", documentId="+documentId+", indexFields="+indexFields+"]", ex);
            throw new DaoException("Error while indexing document into ElasticSearch: " + ex.getMessage());
        }
    }


    @Override
    public Metadata searchById(String indexName, String id) throws DaoException, NotFoundException {
        LOGGER.debug("Search in ElasticSearch by ID [indexName="+indexName+", id="+id+"] ...");
        
        // Validation
        if(Strings.isEmpty(indexName)) throw new IllegalArgumentException("indexName cannot be null or empty");
        if(Strings.isEmpty(id)) throw new IllegalArgumentException("id cannot be null or empty");
        
        try {
            GetResponse response = client.prepareGet(indexName.toLowerCase(), indexName.toLowerCase(), id).get();

            LOGGER.trace("Search one document in ElasticSearch [indexName="+indexName+", id="+id+"] : response=" + response);
            
            if(!response.isExists()) {
                throw new NotFoundException("Document [indexName="+indexName+", id="+id+"] not found");
            }
            
            Metadata metadata = new Metadata(
                    response.getIndex(),
                    response.getId(),
                    response.getSourceAsMap().get(HASH_INDEX_KEY).toString(),
                    response.getSourceAsMap().get(CONTENT_TYPE_INDEX_KEY).toString(),
                    convert(response.getSourceAsMap()));
            
            LOGGER.debug("Search one document in ElasticSearch [indexName="+indexName+", id="+id+"] : " + metadata);

            return metadata;
            
        } catch(NotFoundException ex) {
            LOGGER.warn("Error while searching into ElasticSearch [indexName="+indexName+", id="+id+"]", ex);
            throw ex;
        } catch(Exception ex) {
            LOGGER.debug("Error while searching into ElasticSearch [indexName="+indexName+", id="+id+"]", ex);
            throw new DaoException("Error while searching into ElasticSearch: " + ex.getMessage());
        }
    }
    
    
    @Override
    public List<Metadata> search(Pageable pageable, String indexName, Query query) throws DaoException {
        LOGGER.debug("Search documents in ElasticSearch [indexName="+indexName+", query="+query+"] ...");

        // Validation
        if(pageable == null) throw new IllegalArgumentException("pageable cannot be null");
        if(Strings.isEmpty(indexName)) throw new IllegalArgumentException("indexName cannot be null or empty");
        
        try {
            

            int p = (pageable.getPageNumber() < 0) ? 1 : pageable.getPageNumber();
            int l = (pageable.getPageSize() < 1) ? 1 : pageable.getPageSize();
            
            SearchRequestBuilder requestBuilder = client.prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(convertQuery(query))
                    .setFrom((p-1)*l) 
                    .setSize(l);
    
            if(pageable.getSort() != null) {
                Iterator<Order> orderIterator = pageable.getSort().iterator();
                while (orderIterator.hasNext()) {
                    Order order = orderIterator.next();
                    requestBuilder.addSort(new FieldSortBuilder(order.getProperty()).order(order.isAscending() ? SortOrder.ASC : SortOrder.DESC).unmappedType("date"));
                }
            }
            
            SearchResponse searchResponse = requestBuilder.execute().actionGet();

            LOGGER.trace("Search documents in ElasticSearch [query="+query+"] : " + searchResponse);

            List<Metadata> result = Arrays.stream(searchResponse.getHits().getHits())
                    .map(hit -> {
                        return new Metadata(
                                hit.getIndex(),
                                hit.getId(),
                                hit.getSourceAsMap().get(HASH_INDEX_KEY).toString(),
                                hit.getSourceAsMap().get(CONTENT_TYPE_INDEX_KEY).toString(),
                                convert(hit.getSourceAsMap()));
                    })
                    .collect(Collectors.toList());
            
            
            LOGGER.debug("Search documents in ElasticSearch [indexName="+indexName+", query="+query+"] : " + result);
            
            return result;
            
        } catch(Exception ex) {
            LOGGER.debug("Error while searching documents into ElasticSearch [query="+query+"]", ex);
            throw new DaoException("Error while searching documents into ElasticSearch: " + ex.getMessage());
        }
    }

    @Override
    public long count(String indexName, Query query) throws DaoException {
        LOGGER.debug("Count in ElasticSearch [indexName="+indexName+", query="+query+"] ...");

        // Validation
        if(Strings.isEmpty(indexName)) throw new IllegalArgumentException("indexName cannot be null or empty");
        
        try {
            SearchResponse countResponse = client.prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(convertQuery(query))
                    .setSize(0)
                    .get();

            LOGGER.trace("Count in ElasticSearch [query="+query+"] : " + countResponse);

            return countResponse.getHits().getTotalHits();
            
        } catch(Exception ex) {
            LOGGER.debug("Error while counting into ElasticSearch [query="+query+"]", ex);
            throw new DaoException("Error while counting into ElasticSearch: " + ex.getMessage());
        }
    }

    @Override
    public void createIndex(String indexName) throws DaoException {
        LOGGER.info("Create index in ElasticSearch [indexName="+indexName+"] ...");

        // Validation
        if(Strings.isEmpty(indexName)) throw new IllegalArgumentException("indexName cannot be null or empty");
        
        try {
            boolean exists = client.admin().indices()
                    .prepareExists(indexName)
                    .execute().actionGet().isExists();
            
            if(!exists) {
                client.admin().indices().prepareCreate(indexName).get();
                LOGGER.info("Index created in ElasticSearch [indexName="+indexName+"]");

            } else {
                LOGGER.info("Index already exists in ElasticSearch [indexName="+indexName+"]");
            }

        } catch(Exception ex) {
            LOGGER.debug("Error while creating the index into ElasticSearch [indexName="+indexName+"]", ex);
            throw new DaoException("Error while creating the index into ElasticSearch: " + ex.getMessage());
        }
    }
    
    /**
     * Check if a document exists in E.S.
     * 
     * @param index     Index Name
     * @param id        Document ID
     * @return          true/false
     * 
     * @throws ElasticsearchException
     */
    private Boolean doesExist(String index, String id) throws ElasticsearchException {
        if(id == null || id.isEmpty()) {
            return false;
        }
        
        GetResponse response = client.prepareGet(index, index, id).setRefresh(true).get();
        return response.isExists();
    }
    
    /**
     * Convert a list of IndexField (key/value) to a Map
     * @param indexFields   List of IndexField
     * @return              Map
     */
    private static Map<String, Object> convert(List<IndexField> indexFields) {
        if(indexFields == null) {
            return null;
        }
        
        Map<String, Object> result = indexFields
                .stream()
                .collect(Collectors.toMap(
                        field -> field.getName(), 
                        field -> field.getValue()
                 ));

        return result;
    }
    
    /**
     * Convert a Map to a list of IndexField (key/value)
     * @param indexFields   Map
     * @return              List of IndexField
     */
    private static List<IndexField> convert(Map<String, Object> indexFields) {
        if(indexFields == null) {
            return null;
        }
        
        List<IndexField> result = indexFields.entrySet().stream().map(field -> {
            return new IndexField(field.getKey(), field.getValue());
        }).collect(Collectors.toList());

        return result;
    }
    
    /**
     * Convert a IPFS-Store Query to a ElasticSearch query
     * @param query     IPFS-Store Query 
     * @return          ElasticSearch query
     */
    private static QueryBuilder convertQuery(Query query) {
        LOGGER.trace("Converting query: " +query);
        
        BoolQueryBuilder elasticSearchQuery = QueryBuilders.boolQuery();
        
        if(query == null || query.getFilterClauses().size() == 0) {
            return QueryBuilders.matchAllQuery();
        }
        
        query.getFilterClauses().stream().forEach(f -> {
            try {

                switch(f.getOperation()) {
                case full_text:
                    elasticSearchQuery.must(QueryBuilders.multiMatchQuery(f.getValue(), f.getNames()).lenient(true));
                    break;  
                case equals:
                    elasticSearchQuery.must(QueryBuilders.termQuery(f.getName(), f.getValue()));
                    break;  
                case not_equals:
                    elasticSearchQuery.mustNot(QueryBuilders.termQuery(f.getName(), f.getValue()));
                    break;  
                case contains:
                    elasticSearchQuery.must(QueryBuilders.matchQuery(f.getName(), f.getValue()));
                    break;  
                case in:
                    elasticSearchQuery.filter(QueryBuilders.termsQuery(
                            f.getName(), 
                            Arrays.asList((Object[])f.getValue()).stream().map((o)->o.toString().toLowerCase()).collect(Collectors.toList())));
                    break;  
                case lt:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).lt(f.getValue()));
                    break;  
                case lte:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).lte(f.getValue()));
                    break;  
                case gt:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).gt(f.getValue()));
                    break;  
                case gte:
                    elasticSearchQuery.must(QueryBuilders.rangeQuery(f.getName()).gte(f.getValue()));
                    break; 
                default:
                    LOGGER.warn("Operation ["+f.getOperation()+"] not supported for  filter ["+f+"]- Ignore it!");
                    break;
                }
                
            } catch(Exception e) {
                LOGGER.warn("Error while converting filter ["+f+"] - Ignore it!", e);
            }
        });
        
        LOGGER.debug(elasticSearchQuery.toString());
        
        return elasticSearchQuery;
    }
}
