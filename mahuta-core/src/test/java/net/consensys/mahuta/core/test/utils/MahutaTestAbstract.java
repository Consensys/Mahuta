package net.consensys.mahuta.core.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IndexingRequestAndMetadata;

public abstract class MahutaTestAbstract extends TestUtils {

    protected final IndexingService indexingService;
    protected final StorageService storageService;
    protected final Mahuta mahuta;   
    
    public MahutaTestAbstract(IndexingService indexingService, StorageService storageService) {
        this.indexingService = indexingService;
        this.storageService = storageService;
        this.mahuta = new MahutaFactory().configureStorage(storageService).configureIndexer(indexingService).build();
    }
    
    protected void creatIndex(String indexName) throws Exception {
        
        ////////////////////////
        mahuta.createIndex(indexName);
        List<String> indexes = mahuta.getIndexes();
        ///////////////////////
        
        assertTrue(indexes.stream().filter(i->i.equalsIgnoreCase(indexName)).findFirst().isPresent());
    }
    
    protected void index(IndexingRequestAndMetadata requestAndMedata) throws Exception {
        
        ////////////////////////
        Metadata metadata = mahuta.index(requestAndMedata.getRequest());
        ///////////////////////
        
        validateMetadata(requestAndMedata, metadata);
    }
    
    protected void deindex(IndexingRequestAndMetadata requestAndMedata) throws Exception {
        
        ////////////////////////
        Metadata metadata = mahuta.index(requestAndMedata.getRequest());
        mahuta.deindex(metadata.getIndexName(), metadata.getIndexDocId());
        ///////////////////////

        assertFalse(storageService.getPinned().stream().anyMatch(h -> h.equals(requestAndMedata.getMetadata().getContentId())));
    }
    
    protected void getById(IndexingRequestAndMetadata requestAndMedata) {
        
        ////////////////////////
        Metadata metadata = mahuta.index(requestAndMedata.getRequest());
        MetadataAndPayload metadataAndPayload = mahuta.getById(requestAndMedata.getRequest().getIndexName(), requestAndMedata.getRequest().getIndexDocId());
        ////////////////////////

        assertNotNull(metadataAndPayload.getPayload());
        validateMetadata(requestAndMedata, metadata);
        validateMetadata(requestAndMedata, metadataAndPayload.getMetadata());
    }
    
    protected void getByHash(IndexingRequestAndMetadata requestAndMedata) {
        
        ////////////////////////
        Metadata metadata = mahuta.index(requestAndMedata.getRequest());
        MetadataAndPayload metadataAndPayload = mahuta.getByHash(requestAndMedata.getRequest().getIndexName(), requestAndMedata.getMetadata().getContentId());
        ////////////////////////

        assertNotNull(metadataAndPayload.getPayload());
        validateMetadata(requestAndMedata, metadata);
        validateMetadata(requestAndMedata, metadataAndPayload.getMetadata());
    }

    protected void searchAll(List<IndexingRequestAndMetadata> requestAndMedata, Integer expectedNoResult) {
        this.search(requestAndMedata, null, expectedNoResult); 
    }

    protected void search(List<IndexingRequestAndMetadata> requestAndMedata, Query query, Integer expectedNoResult) {
        this.search(requestAndMedata, query, expectedNoResult, null); 
    }
    
    protected void search(List<IndexingRequestAndMetadata> requestAndMedata, Query query, Integer expectedNoResult, IndexingRequestAndMetadata expectedFirstResult) {
        
        ////////////////////////
        requestAndMedata.forEach(i->mahuta.index(i.getRequest()));
        Page<Metadata> result = mahuta.search(requestAndMedata.get(0).getRequest().getIndexName(), query);
        ////////////////////////

        assertEquals(expectedNoResult, result.getTotalElements());
        
        if(expectedFirstResult != null) {
            validateMetadata(expectedFirstResult, result.getElements().get(0));
            
        }
    }
    
    public static void validateMetadata(IndexingRequestAndMetadata requestAndMedata, Metadata metadata) {
        assertTrue(requestAndMedata.getMetadata().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        
        if(requestAndMedata.getRequest().getIndexDocId() != null) {
            assertEquals(requestAndMedata.getMetadata().getIndexDocId(), metadata.getIndexDocId());
        } else {
            assertNotNull(metadata.getIndexDocId());
        }
        
        assertEquals(requestAndMedata.getMetadata().getContentId(), metadata.getContentId());
        assertEquals(requestAndMedata.getMetadata().getContentType(), metadata.getContentType());
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.TITLE_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.STATUS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.STATUS_FIELD));
        
    }

}
