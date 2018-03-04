package net.consensys.tools.ipfs.ipfsstore.test.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.assertj.core.util.Arrays;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dao.impl.ElasticSearchIndexDao;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
public class ElasticSearchDAOTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchDAOTest.class);
    
    private IndexDao underTest;
    private ObjectMapper mapper = new ObjectMapper();
    
    @MockBean
    private TransportClient client;
    
    private String indexName = "myIndex";
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        underTest = new ElasticSearchIndexDao(client);
    }
    
    public static List<IndexField> getIndexFields(String key, String value) {
        List<IndexField> result = new ArrayList<>();
        IndexField i1 = new IndexField("external_id", "111222");
        IndexField i2 = new IndexField("title", "hello doc");
        IndexField i3 = new IndexField("votes", 5);
        IndexField i4 = new IndexField(key, value);
        
        result.add(i1);
        result.add(i2);
        result.add(i3);
        result.add(i4);
        
        return result;
    }
    
    
    

    // #########################################################
    // ####################### index
    // #########################################################
    
    @Test
    public void indexCreateSuccessTest() throws DaoException, JsonParseException, JsonMappingException, IOException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        
        // Mock
        GetResponse getResponse = mock(GetResponse.class);
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.setRefresh(eq(true))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.get()).thenReturn(getResponse);
        Mockito.when(getResponse.isExists()).thenReturn(false);
        
        IndexResponse indexResponse = mock(IndexResponse.class);
        IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);
        Mockito.when(client.prepareIndex(anyString(), anyString(), eq(documentId))).thenReturn(indexRequestBuilder);
        Mockito.when(indexRequestBuilder.setSource(any(String.class), eq(XContentType.JSON))).thenReturn(indexRequestBuilder);
        Mockito.when(indexRequestBuilder.get()).thenReturn(indexResponse);
        Mockito.when(indexResponse.getId()).thenReturn(documentId);

        // #################################################
        String docReturned = underTest.index(indexName, documentId, hash, contentType, getIndexFields(customAttributeKey, customAttributeVal));
        // #################################################

        ArgumentCaptor<String> argumentCaptorSource = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorDocumentId = ArgumentCaptor.forClass(String.class);
        Mockito.verify(client, Mockito.times(1)).prepareIndex(argumentCaptorIndexName.capture(), argumentCaptorIndexType.capture(), argumentCaptorDocumentId.capture()); 
        Mockito.verify(indexRequestBuilder, Mockito.times(1)).setSource(argumentCaptorSource.capture(), eq(XContentType.JSON));
        Mockito.verify(indexRequestBuilder, Mockito.times(1)).get(); 
        
        String sourceCaptured = argumentCaptorSource.<Map> getValue();
        Map<String, Object> source = mapper.readValue(sourceCaptured, new TypeReference<Map<String, Object>>() {});
        String indexNameCaptured = argumentCaptorIndexName.<String> getValue();
        String indexTypeCaptured = argumentCaptorIndexType.<String> getValue();
        String documentIdCaptured = argumentCaptorDocumentId.<String> getValue();
        
        assertEquals(source.get(IndexDao.HASH_INDEX_KEY), hash);
        assertEquals(source.get(IndexDao.CONTENT_TYPE_INDEX_KEY), contentType);
        assertEquals(source.get(customAttributeKey), customAttributeVal);
        assertEquals(indexNameCaptured, indexName.toLowerCase());
        assertEquals(indexTypeCaptured, indexName.toLowerCase());
        assertEquals(documentIdCaptured, documentId);
        assertEquals(docReturned, documentId);
        
    }
    
    @Test
    public void indexCreateSuccessNoAttributeTest() throws DaoException, JsonParseException, JsonMappingException, IOException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        
        
        // Mock
        GetResponse getResponse = mock(GetResponse.class);
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.setRefresh(eq(true))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.get()).thenReturn(getResponse);
        Mockito.when(getResponse.isExists()).thenReturn(false);
        
        IndexResponse indexResponse = mock(IndexResponse.class);
        IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);
        Mockito.when(client.prepareIndex(anyString(), anyString(), eq(documentId))).thenReturn(indexRequestBuilder);
        Mockito.when(indexRequestBuilder.setSource(any(String.class), eq(XContentType.JSON))).thenReturn(indexRequestBuilder);
        Mockito.when(indexRequestBuilder.get()).thenReturn(indexResponse);
        Mockito.when(indexResponse.getId()).thenReturn(documentId);

        // #################################################
        String docReturned = underTest.index(indexName, documentId, hash, contentType, null);
        // #################################################

        ArgumentCaptor<String> argumentCaptorSource = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorDocumentId = ArgumentCaptor.forClass(String.class);
        Mockito.verify(client, Mockito.times(1)).prepareIndex(argumentCaptorIndexName.capture(), argumentCaptorIndexType.capture(), argumentCaptorDocumentId.capture()); 
        Mockito.verify(indexRequestBuilder, Mockito.times(1)).setSource(argumentCaptorSource.capture(), eq(XContentType.JSON));
        Mockito.verify(indexRequestBuilder, Mockito.times(1)).get(); 
        
        String sourceCaptured = argumentCaptorSource.<String> getValue();
        Map<String, Object> source = mapper.readValue(sourceCaptured, new TypeReference<Map<String, Object>>() {});
        String indexNameCaptured = argumentCaptorIndexName.<String> getValue();
        String indexTypeCaptured = argumentCaptorIndexType.<String> getValue();
        String documentIdCaptured = argumentCaptorDocumentId.<String> getValue();
        
        assertEquals(source.get(IndexDao.HASH_INDEX_KEY), hash);
        assertEquals(source.get(IndexDao.CONTENT_TYPE_INDEX_KEY), contentType);
        assertEquals(indexNameCaptured, indexName.toLowerCase());
        assertEquals(indexTypeCaptured, indexName.toLowerCase());
        assertEquals(documentIdCaptured, documentId);
        assertEquals(docReturned, documentId);
        
    }
    
    @Test
    public void indexUpdateSuccessTest() throws DaoException, JsonParseException, JsonMappingException, IOException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        
        // Mock
        GetResponse getResponse = mock(GetResponse.class);
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.setRefresh(eq(true))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.get()).thenReturn(getResponse);
        Mockito.when(getResponse.isExists()).thenReturn(true);
        
        UpdateResponse indexResponse = mock(UpdateResponse.class);
        UpdateRequestBuilder indexRequestBuilder = mock(UpdateRequestBuilder.class);
        Mockito.when(client.prepareUpdate(anyString(), anyString(), eq(documentId))).thenReturn(indexRequestBuilder);
        Mockito.when(indexRequestBuilder.setDoc(any(String.class), eq(XContentType.JSON))).thenReturn(indexRequestBuilder);
        Mockito.when(indexRequestBuilder.get()).thenReturn(indexResponse);
        Mockito.when(indexResponse.getId()).thenReturn(documentId);

        // #################################################
        String docReturned = underTest.index(indexName, documentId, hash, contentType, getIndexFields(customAttributeKey, customAttributeVal));
        // #################################################

        ArgumentCaptor<String> argumentCaptorSource = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorDocumentId = ArgumentCaptor.forClass(String.class);
        Mockito.verify(client, Mockito.times(1)).prepareUpdate(argumentCaptorIndexName.capture(), argumentCaptorIndexType.capture(), argumentCaptorDocumentId.capture()); 
        Mockito.verify(indexRequestBuilder, Mockito.times(1)).setDoc(argumentCaptorSource.capture(), eq(XContentType.JSON));
        Mockito.verify(indexRequestBuilder, Mockito.times(1)).get(); 
        
        String sourceCaptured = argumentCaptorSource.<Map> getValue();
        Map<String, Object> source = mapper.readValue(sourceCaptured, new TypeReference<Map<String, Object>>() {});
        String indexNameCaptured = argumentCaptorIndexName.<String> getValue();
        String indexTypeCaptured = argumentCaptorIndexType.<String> getValue();
        String documentIdCaptured = argumentCaptorDocumentId.<String> getValue();
        
        assertEquals(source.get(IndexDao.HASH_INDEX_KEY), hash);
        assertEquals(source.get(IndexDao.CONTENT_TYPE_INDEX_KEY), contentType);
        assertEquals(source.get(customAttributeKey), customAttributeVal);
        assertEquals(indexNameCaptured, indexName.toLowerCase());
        assertEquals(indexTypeCaptured, indexName.toLowerCase());
        assertEquals(documentIdCaptured, documentId);
        assertEquals(docReturned, documentId);
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void indexCreateKOIllegalArgumentsTest1() throws IOException, DaoException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // #################################################
        String docReturned = underTest.index(null, documentId, hash, contentType, getIndexFields(customAttributeKey, customAttributeVal));
        // ################################################# 
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void indexCreateKOIllegalArgumentsTest2() throws IOException, DaoException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // #################################################
        String docReturned = underTest.index(indexName, documentId, null, contentType, getIndexFields(customAttributeKey, customAttributeVal));
        // ################################################# 
    }
    

    @Test(expected=DaoException.class)
    public void indexCreateUnexpectedExceptionTest() throws DaoException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        
        // Mock
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenThrow(new RuntimeException());

        // #################################################
        underTest.index(indexName, documentId, hash, contentType, getIndexFields(customAttributeKey, customAttributeVal));
        // #################################################
        
    }
    
    

    // #########################################################
    // ####################### searchById
    // #########################################################
    
    @Test
    public void searchByIdSuccessTest() throws DaoException {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);
        
        GetResponse getResponse = mock(GetResponse.class);
        Mockito.when(getResponse.getSourceAsMap()).thenReturn(sourceMap);
        Mockito.when(getResponse.getId()).thenReturn(documentId);
        Mockito.when(getResponse.getIndex()).thenReturn(indexName);
        Mockito.when(getResponse.isExists()).thenReturn(true);
        
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.get()).thenReturn(getResponse);
        
        // #################################################
        Metadata meta = underTest.searchById(indexName, documentId);
        // #################################################
        
        ArgumentCaptor<String> argumentCaptorIndexName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorIndexType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorDocumentId = ArgumentCaptor.forClass(String.class);
        Mockito.verify(client, Mockito.times(1)).prepareGet(argumentCaptorIndexName.capture(), argumentCaptorIndexType.capture(), argumentCaptorDocumentId.capture()); 
        Mockito.verify(getRequestBuilder, Mockito.times(1)).get(); 
        
        String indexNameCaptured = argumentCaptorIndexName.<String> getValue();
        String indexTypeCaptured = argumentCaptorIndexType.<String> getValue();
        String documentIdCaptured = argumentCaptorDocumentId.<String> getValue();
        
        assertEquals(indexNameCaptured, indexName.toLowerCase());
        assertEquals(indexTypeCaptured, indexName.toLowerCase());
        assertEquals(documentIdCaptured, documentId);
        
        
        assertEquals(documentId, meta.getDocumentId());
        assertEquals(indexName, meta.getIndexName());
        assertEquals(hash, meta.getHash()); 
    }

    @Test(expected=IllegalArgumentException.class)
    public void searchByIdKOIllegalArgumentsTest1() throws IOException, DaoException {
        String documentId = null;
        
        // #################################################
        underTest.searchById(indexName, documentId);
        // ################################################# 
    }

    @Test(expected=IllegalArgumentException.class)
    public void searchByIdKOIllegalArgumentsTest2() throws IOException, DaoException {
        String documentId = "123";
        
        // #################################################
        underTest.searchById("", documentId);
        // ################################################# 
    }
    
    
    @Test(expected=NotFoundException.class)
    public void searchByIdNotFoundExceptionTest() throws DaoException {
    
        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);
        
        GetResponse getResponse = mock(GetResponse.class);
        Mockito.when(getResponse.getSourceAsMap()).thenReturn(sourceMap);
        Mockito.when(getResponse.getId()).thenReturn(documentId);
        Mockito.when(getResponse.getIndex()).thenReturn(indexName);
        Mockito.when(getResponse.isExists()).thenReturn(false);
        
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.get()).thenReturn(getResponse);
    
        // #################################################
        underTest.searchById(indexName, documentId);
        // #################################################
        
    }
    
    
    @Test(expected=DaoException.class)
    public void searchByIdUnexpectedExceptionTest() throws DaoException {
    
        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);
        
        GetResponse getResponse = mock(GetResponse.class);
        Mockito.when(getResponse.getSourceAsMap()).thenReturn(sourceMap);
        Mockito.when(getResponse.getId()).thenReturn(documentId);
        Mockito.when(getResponse.getIndex()).thenReturn(indexName);
        Mockito.when(getResponse.isExists()).thenReturn(true);
        
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        Mockito.when(client.prepareGet(anyString(), anyString(), eq(documentId))).thenReturn(getRequestBuilder);
        Mockito.when(getRequestBuilder.get()).thenThrow(new RuntimeException());
    
        // #################################################
        underTest.searchById(indexName, documentId);
        // #################################################

    }
    
    


    // #########################################################
    // ####################### search
    // #########################################################
      

    @Test
    public void searchSuccessNullQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        
        
        Pageable pagination = new PageRequest(pageNo, pageSize);
        

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, null);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "   \"match_all\": {\"boost\" : 1.0}" + 
                "}", queryCaptured.toString(), true);
        
        ArgumentCaptor<Integer> argumentCaptorFrom = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setFrom(argumentCaptorFrom.capture()); 
        Integer fromCaptured = argumentCaptorFrom.<Integer> getValue();
        assertEquals(Integer.valueOf(pageNo-1), fromCaptured);
        
        
        ArgumentCaptor<Integer> argumentCaptorSize = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setSize(argumentCaptorFrom.capture()); 
        Integer sizeCaptured = argumentCaptorFrom.<Integer> getValue();
        assertEquals(Integer.valueOf(pageSize), sizeCaptured);
        
        //TODO Sort
        
        Mockito.verify(listenableActionFuture, Mockito.times(1)).actionGet(); 
        
        assertEquals("Search result count should be 1", 1, searchResult.size());
        assertEquals(documentId, searchResult.get(0).getDocumentId());
        assertEquals(hash, searchResult.get(0).getHash());
        assertEquals(contentType, searchResult.get(0).getContentType());
        
    }

    @Test
    public void searchSuccessFullTextQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);
        
        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        Query query = new Query();
        query.fullText(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        
        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"multi_match\" : {\n" + 
                "        \"query\" : \""+customAttributeVal+"\",\n" + 
                "        \"fields\" : [ \""+customAttributeKey+"^1.0\"  ],\n" + // ^1.0 ??? WTF 
                "        \"lenient\" : true\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }

    @Test
    public void searchSuccessEqualsQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        Query query = new Query();
        query.equals(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"term\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"value\" : \""+customAttributeVal+"\"\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }

    @Test
    public void searchSuccessNotEqualsQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        Query query = new Query();
        query.notEquals(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must_not\" : [{\n" + 
                "      \"term\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"value\" : \""+customAttributeVal+"\"\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }

    @Test
    public void searchSuccessContainsQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        Query query = new Query();
        query.contains(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"match\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"query\" : \""+customAttributeVal+"\"\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }

    @Test
    public void searchSuccessInQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal1 = "test123";
        String customAttributeVal2 = "test456";
        
        Query query = new Query();
        query.in(customAttributeKey, customAttributeVal1, customAttributeVal2);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal1);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"filter\" : [{\n" + 
                "      \"terms\" : {\n" + 
                "           \""+customAttributeKey+"\" : [\n" + 
                "               \""+customAttributeVal1+"\", \""+customAttributeVal2+"\"\n" + 
                "           ]\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }

    @Test
    public void searchSuccessltQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        int customAttributeVal = 10;
        
        Query query = new Query();
        query.lessThan(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"range\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"to\" : "+customAttributeVal+",\n" + 
                "               \"include_upper\" : false\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }
    
    @Test
    public void searchSuccesslteQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        int customAttributeVal = 10;
        
        Query query = new Query();
        query.lessThanOrEquals(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"range\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"to\" : "+customAttributeVal+",\n" + 
                "               \"include_upper\" : true\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }
    
    @Test
    public void searchSuccessgtQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        int customAttributeVal = 10;
        
        Query query = new Query();
        query.greaterThan(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"range\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"from\" : "+customAttributeVal+",\n" + 
                "               \"include_lower\" : false\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }
    
    @Test
    public void searchSuccessgteQueryTest() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        Pageable pagination = new PageRequest(pageNo, pageSize);

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        int customAttributeVal = 10;
        
        Query query = new Query();
        query.greaterThanOrEquals(customAttributeKey, customAttributeVal);
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        List<Metadata> searchResult = underTest.search(pagination, indexName, query);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "  \"bool\" : {\n" + 
                "    \"must\" : [{\n" + 
                "      \"range\" : {\n" + 
                "           \""+customAttributeKey+"\" : {\n" + 
                "               \"from\" : "+customAttributeVal+",\n" + 
                "               \"include_lower\" : true\n" + 
                "           }\n" + 
                "      }\n" + 
                "    }]\n" + 
                "  }\n" + 
                "}", queryCaptured.toString(), false);
        
    }
    


    @Test(expected=IllegalArgumentException.class)
    public void searchSuccessNullQueryIllegalArgumentExceptionTest1() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        
        
        Pageable pagination = new PageRequest(pageNo, pageSize);
        

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        underTest.search(null, indexName, null);
        // #################################################

        
    }
    

    @Test(expected=IllegalArgumentException.class)
    public void searchSuccessNullQueryIllegalArgumentExceptionTest2() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        
        
        Pageable pagination = new PageRequest(pageNo, pageSize);
        

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        
        // #################################################
        underTest.search(pagination, null, null);
        // #################################################
   
    }
    

    

    @Test(expected=DaoException.class)
    public void searchSuccessNullQueryUnexpectedxceptionTest1() throws DaoException, JSONException {
        int pageNo = 1;
        int pageSize = 20;
        
        
        Pageable pagination = new PageRequest(pageNo, pageSize);
        

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/pdf";
        String documentId = "123";
        String customAttributeKey = "test";
        String customAttributeVal = "test123";
        
        // Mock
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(IndexDao.HASH_INDEX_KEY, hash);
        sourceMap.put(IndexDao.CONTENT_TYPE_INDEX_KEY, contentType);
        sourceMap.put(customAttributeKey, customAttributeVal);

        SearchHit searchHit1 = mock(SearchHit.class);
        when(searchHit1.getSourceAsMap()).thenReturn(sourceMap);
        when(searchHit1.getId()).thenReturn(documentId);
        when(searchHit1.getType()).thenReturn(indexName);
        when(searchHit1.getIndex()).thenReturn(indexName);

        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(Arrays.array(searchHit1));
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setFrom(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(anyInt())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.addSort(any(FieldSortBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenThrow(new RuntimeException());
        
        // #################################################
        underTest.search(pagination, indexName, null);
        // #################################################
        
    }
    

    // #########################################################
    // ####################### count
    // #########################################################
    
    @Test
    public void countSuccessTest() throws DaoException, JSONException, InterruptedException, ExecutionException {

        long total = 10;
        
        // Mock
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(total);
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(eq(0))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.get()).thenReturn(searchResponse);
        
        // #################################################
        long totalResult = underTest.count(indexName, null);
        // #################################################
        

        Mockito.verify(client, Mockito.times(1)).prepareSearch(anyString()); 
        
        ArgumentCaptor<QueryBuilder> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(QueryBuilder.class);
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).setQuery(argumentCaptorQueryBuilder.capture()); 
        QueryBuilder queryCaptured = argumentCaptorQueryBuilder.<QueryBuilder> getValue();
        LOGGER.debug(queryCaptured.toString());
        JSONAssert.assertEquals("{\n" + 
                "   \"match_all\": {\"boost\" : 1.0}" + 
                "}", queryCaptured.toString(), true);

        
        Mockito.verify(searchRequestBuilder, Mockito.times(1)).get(); 
        
        assertEquals(totalResult, total);
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void countIllegalArgumentExceptionTest() throws DaoException, JSONException, InterruptedException, ExecutionException {

        long total = 10;
        
        // Mock
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(total);
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(eq(0))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.get()).thenReturn(searchResponse);
        
        // #################################################
        underTest.count(null, null);
        // #################################################

        
    }
    
    @Test(expected=DaoException.class)
    public void countUnexpectedExceptionTest() throws DaoException, JSONException, InterruptedException, ExecutionException {

        long total = 10;
        
        // Mock
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(total);
        
        SearchResponse searchResponse = mock(SearchResponse.class);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        Mockito.when(client.prepareSearch(anyString())).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSearchType(eq(SearchType.DFS_QUERY_THEN_FETCH))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setQuery(any(QueryBuilder.class))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.setSize(eq(0))).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.get()).thenThrow(new RuntimeException());
        
        // #################################################
        underTest.count(indexName, null);
        // #################################################

        
    }
    
    

    // #########################################################
    // ####################### createdIndex
    // #########################################################
    
    // Need to use PowerMockito to mock a final method
//    @Test
//    public void createIndexSuccessTest() throws DaoException, JSONException, InterruptedException, ExecutionException {
//
//        long total = 10;
//        
//        // Mock
//
//        IndicesExistsResponse indicesExistsResponse = mock(IndicesExistsResponse.class);
//        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
//        IndicesExistsRequestBuilder IndicesExistsRequestBuilder = mock(IndicesExistsRequestBuilder.class);
//        IndicesAdminClient indicesAdminClient = mock(IndicesAdminClient.class);
//        AdminClient adminClient = mock(AdminClient.class);
//        Mockito.when(client.admin()).thenReturn(adminClient);
//        Mockito.when(adminClient.indices()).thenReturn(indicesAdminClient);
//        Mockito.when(indicesAdminClient.prepareExists(any(String.class))).thenReturn(IndicesExistsRequestBuilder);
//        Mockito.when(IndicesExistsRequestBuilder.execute()).thenReturn(listenableActionFuture);
//        Mockito.when(listenableActionFuture.actionGet()).thenReturn(indicesExistsResponse);
//        Mockito.when(indicesExistsResponse.isExists()).thenReturn(false);
//        
//
//        CreateIndexResponse  createIndexResponse  = mock(CreateIndexResponse.class);
//        CreateIndexRequestBuilder createIndexRequestBuilder = mock(CreateIndexRequestBuilder.class);
//        Mockito.when(indicesAdminClient.prepareCreate(any(String.class))).thenReturn(createIndexRequestBuilder);
//        Mockito.when(createIndexRequestBuilder.get()).thenReturn(createIndexResponse);
//        
//        // #################################################
//        underTest.createIndex(indexName);
//        // #################################################
//        
//        ArgumentCaptor<String> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(String.class);
//        Mockito.verify(indicesAdminClient, Mockito.times(1)).prepareCreate(argumentCaptorQueryBuilder.capture()); 
//        String indexNameCaptured = argumentCaptorQueryBuilder.<String> getValue();
//        assertEquals(indexName, indexNameCaptured);
//        
//        
//    }
    
//    @Test
//    public void createIndexAlreadyExistTest() throws DaoException, JSONException, InterruptedException, ExecutionException {
//
//        long total = 10;
//        
//        // Mock
//
//        IndicesExistsResponse indicesExistsResponse = mock(IndicesExistsResponse.class);
//        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
//        IndicesExistsRequestBuilder IndicesExistsRequestBuilder = mock(IndicesExistsRequestBuilder.class);
//        IndicesAdminClient indicesAdminClient = mock(IndicesAdminClient.class);
//        AdminClient adminClient = mock(AdminClient.class);
//        Mockito.when(client.admin()).thenReturn(adminClient);
//        Mockito.when(adminClient.indices()).thenReturn(indicesAdminClient);
//        Mockito.when(indicesAdminClient.prepareExists(any(String.class))).thenReturn(IndicesExistsRequestBuilder);
//        Mockito.when(IndicesExistsRequestBuilder.execute()).thenReturn(listenableActionFuture);
//        Mockito.when(listenableActionFuture.actionGet()).thenReturn(indicesExistsResponse);
//        Mockito.when(indicesExistsResponse.isExists()).thenReturn(true);
//        
//        // #################################################
//        underTest.createIndex(indexName);
//        // #################################################
//        
//    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createIndexIllegalArgumentExceptionTest() throws DaoException, JSONException, InterruptedException, ExecutionException {

        // #################################################
        underTest.createIndex(null);
        // #################################################

        
    }
    
//    @Test(expected=DaoException.class)
//    public void createIndexUnexpectedExceptionTest() throws DaoException, JSONException, InterruptedException, ExecutionException {
//
//        long total = 10;
//        
//        // Mock
//        IndicesExistsResponse indicesExistsResponse = mock(IndicesExistsResponse.class);
//        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
//        IndicesExistsRequestBuilder IndicesExistsRequestBuilder = mock(IndicesExistsRequestBuilder.class);
//        IndicesAdminClient indicesAdminClient = mock(IndicesAdminClient.class);
//        AdminClient adminClient = mock(AdminClient.class);
//        Mockito.when(client.admin()).thenReturn(adminClient);
//        Mockito.when(adminClient.indices()).thenReturn(indicesAdminClient);
//        Mockito.when(indicesAdminClient.prepareExists(any(String.class))).thenReturn(IndicesExistsRequestBuilder);
//        Mockito.when(IndicesExistsRequestBuilder.execute()).thenReturn(listenableActionFuture);
//        Mockito.when(listenableActionFuture.actionGet()).thenReturn(indicesExistsResponse);
//        Mockito.when(indicesExistsResponse.isExists()).thenThrow(new RuntimeException());
//
//        // #################################################
//        underTest.createIndex(indexName);
//        // #################################################
//
//        
//    }
}
