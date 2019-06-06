package net.consensys.mahuta.core.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.Response.ResponseStatus;
import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.createindex.CreateIndexResponse;
import net.consensys.mahuta.core.domain.deindexing.DeindexingResponse;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;

public abstract class MahutaTestAbstract extends TestUtils {

    protected final IndexingService indexingService;
    protected final StorageService storageService;
    protected final Mahuta mahuta;   
    
    public MahutaTestAbstract(IndexingService indexingService, StorageService storageService) {
        this.indexingService = indexingService;
        this.storageService = storageService;
        
        this.mahuta = new MahutaFactory()
                .configureStorage(storageService)
                .configureIndexer(indexingService)
                .defaultImplementation();
    }
    
    protected void creatIndex(String indexName) throws Exception {

        ////////////////////////
        CreateIndexResponse createIndexResponse = mahuta.prepareCreateIndex(indexName).execute();
        ///////////////////////
        assertEquals(ResponseStatus.SUCCESS, createIndexResponse.getStatus());
        
        GetIndexesResponse getIndexesResponse = mahuta.prepareGetIndexes().execute();
        assertEquals(ResponseStatus.SUCCESS, getIndexesResponse.getStatus());
        assertTrue(getIndexesResponse.getIndexes().stream().filter(i->i.equalsIgnoreCase(indexName)).findFirst().isPresent());
    }
    
    protected void index(BuilderAndResponse<IndexingRequest,IndexingResponse> builderAndResponse) throws Exception {
        
        ////////////////////////
        IndexingResponse indexingResponse = builderAndResponse.getBuilder().execute();
        assertEquals(ResponseStatus.SUCCESS, indexingResponse.getStatus());
        ///////////////////////
        
        validateMetadata(builderAndResponse, indexingResponse);
        
        // Check each replica
        Thread.sleep(2000);
        assertTrue(storageService.getReplicaSet().stream()
                .allMatch(p->p.getTracked().contains(builderAndResponse.getResponse().getContentId())));
    }
    
    protected void deindex(BuilderAndResponse<IndexingRequest,IndexingResponse> builderAndResponse) throws Exception {
        
        ////////////////////////
        IndexingResponse indexingResponse = builderAndResponse.getBuilder().execute();
        assertEquals(ResponseStatus.SUCCESS, indexingResponse.getStatus());

        Thread.sleep(2000);
        
        DeindexingResponse deindexingResponse = mahuta.prepareDeindexing(
            builderAndResponse.getBuilder().getRequest().getIndexName(),
            builderAndResponse.getBuilder().getRequest().getIndexDocId())
            .execute();
        assertEquals(ResponseStatus.SUCCESS, deindexingResponse.getStatus());
        ///////////////////////

        // Check each replica
        Thread.sleep(2000);
        assertFalse(storageService.getReplicaSet().stream()
                .anyMatch(p->p.getTracked().contains(builderAndResponse.getResponse().getContentId())));
    }
    
    protected void getById(BuilderAndResponse<IndexingRequest,IndexingResponse> builderAndResponse) {
        
        ////////////////////////
        IndexingResponse indexingResponse = builderAndResponse.getBuilder().execute();
        assertEquals(ResponseStatus.SUCCESS, indexingResponse.getStatus());
        
        GetResponse getResponse = mahuta.prepareGet()
                .indexName(builderAndResponse.getBuilder().getRequest().getIndexName())
                .indexDocId(builderAndResponse.getBuilder().getRequest().getIndexDocId())
                .loadFile(true)
                .execute();
        assertEquals(ResponseStatus.SUCCESS, getResponse.getStatus());
        ////////////////////////

        validateMetadata(builderAndResponse, indexingResponse);
        validateMetadata(builderAndResponse, getResponse.getMetadata());
        assertNotNull(getResponse.getPayload());
    }
    
    protected void getByHash(BuilderAndResponse<IndexingRequest,IndexingResponse> builderAndResponse) {
        
        ////////////////////////
        IndexingResponse indexingResponse = builderAndResponse.getBuilder().execute();
        assertEquals(ResponseStatus.SUCCESS, indexingResponse.getStatus());
        
        GetResponse getResponse = mahuta.prepareGet()
                .indexName(builderAndResponse.getBuilder().getRequest().getIndexName())
                .contentId(builderAndResponse.getResponse().getContentId())
                .loadFile(true)
                .execute();
        ////////////////////////
        
        validateMetadata(builderAndResponse, indexingResponse);
        validateMetadata(builderAndResponse, getResponse.getMetadata());
        assertNotNull(getResponse.getPayload());
    }

    protected void searchAll(List<BuilderAndResponse<IndexingRequest,IndexingResponse>> builderAndResponse, Integer expectedNoResult) {
        this.search(builderAndResponse, null, expectedNoResult); 
    }

    protected void search(List<BuilderAndResponse<IndexingRequest,IndexingResponse>> builderAndResponse, Query query, Integer expectedNoResult) {
        this.search(builderAndResponse, query, expectedNoResult, null); 
    }
    
    protected void search(List<BuilderAndResponse<IndexingRequest,IndexingResponse>> builderAndResponse, Query query, Integer expectedNoResult, BuilderAndResponse<IndexingRequest,IndexingResponse> expectedFirstResult) {
        
        ////////////////////////
        builderAndResponse.forEach(r -> r.getBuilder().execute());
        Page<MetadataAndPayload> result = mahuta.prepareSearch()
                .indexName(builderAndResponse.get(0).getBuilder().getRequest().getIndexName())
                .query(query)
                .execute()
                .getPage();
        ////////////////////////

        assertEquals(expectedNoResult, result.getTotalElements());
        
        if(expectedFirstResult != null) {
            validateMetadata(expectedFirstResult, result.getElements().get(0).getMetadata());
            
        }
    }
    
    protected void updateField(BuilderAndResponse<IndexingRequest,IndexingResponse> builderAndResponse, String field, Object value) {
        
        ////////////////////////
        builderAndResponse.getBuilder().execute();
        
        mahuta.prepareUpdateField(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                field, 
                value).execute();

        GetResponse getResponse = mahuta.prepareGet()
                .indexName(builderAndResponse.getBuilder().getRequest().getIndexName())
                .indexDocId(builderAndResponse.getBuilder().getRequest().getIndexDocId())
                .loadFile(true)
                .execute();
        ////////////////////////

        assertEquals(value, getResponse.getMetadata().getIndexFields().get(field));
    }
    
    public static void validateMetadata(BuilderAndResponse<IndexingRequest, IndexingResponse> builder, Metadata metadata) {
        assertTrue(builder.getResponse().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        
        if(builder.getBuilder().getRequest().getIndexDocId() != null) {
            assertEquals(builder.getResponse().getIndexDocId(), metadata.getIndexDocId());
        } else {
            assertNotNull(metadata.getIndexDocId());
        }
                
        assertEquals(builder.getResponse().getContentId(), metadata.getContentId());
        assertEquals(builder.getResponse().getContentType(), metadata.getContentType());
        assertEquals(builder.getResponse().getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD));
        assertEquals(builder.getResponse().getIndexFields().get(IndexingRequestUtils.TITLE_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD));
        assertEquals(builder.getResponse().getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD));
        assertEquals(builder.getResponse().getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD));
        assertEquals(builder.getResponse().getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD));
        assertEquals(builder.getResponse().getIndexFields().get(IndexingRequestUtils.STATUS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.STATUS_FIELD));
    }
    
    ////////////////////////////////
    

    protected void mockGetIndexes(String indexName) {
        when(indexingService.getIndexes())
        .thenReturn(Arrays.asList(indexName, mockNeat.strings().get(), mockNeat.strings().get()));
    } 
    
    protected void mockIndex(BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse) {
        if(builderAndResponse.getBuilder().getRequest().isIndexContent()) {
            when(indexingService.index(
                    eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                    eq(builderAndResponse.getBuilder().getRequest().getIndexDocId()), 
                    eq(builderAndResponse.getResponse().getContentId()), 
                    eq(builderAndResponse.getBuilder().getRequest().getContentType()), 
                    any(byte[].class),
                    any(boolean.class),
                    eq(builderAndResponse.getBuilder().getRequest().getIndexFields())))
            .thenReturn(builderAndResponse.getResponse().getIndexDocId());
            
        } else {
            when(indexingService.index(
                    eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                    eq(builderAndResponse.getBuilder().getRequest().getIndexDocId()), 
                    eq(builderAndResponse.getResponse().getContentId()), 
                    eq(builderAndResponse.getBuilder().getRequest().getContentType()), 
                    eq(null),
                    any(boolean.class),
                    eq(builderAndResponse.getBuilder().getRequest().getIndexFields())))
            .thenReturn(builderAndResponse.getResponse().getIndexDocId());
            
        }
    }  
    
    protected void mockGetDocument(BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse) {
        when(indexingService.getDocument(
                eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                eq(builderAndResponse.getBuilder().getRequest().getIndexDocId())))
        .thenReturn(builderAndResponse.getResponse());
    } 
    
    protected void mockSearchDocuments(String indexName, Integer totalNo, Query query,  BuilderAndResponse<IndexingRequest, IndexingResponse>... builderAndResponses) {
        when(indexingService.searchDocuments(
                eq(indexName), 
                eq(query),
                any(PageRequest.class)))
        .thenReturn(Page.of(
                PageRequest.of(), 
                Arrays.asList(builderAndResponses).stream().map(b->b.getResponse()).collect(Collectors.toList()), 
                totalNo));
    }    

}
