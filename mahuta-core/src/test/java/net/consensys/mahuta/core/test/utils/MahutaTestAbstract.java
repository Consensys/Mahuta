package net.consensys.mahuta.core.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.Metadata;
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
    
    private void validateMetadata(IndexingRequestAndMetadata requestAndMedata, Metadata metadata) {
        assertEquals(requestAndMedata.getMetadata().getIndexName(), metadata.getIndexName());
        assertEquals(requestAndMedata.getMetadata().getIndexDocId(), metadata.getIndexDocId());
        assertEquals(requestAndMedata.getMetadata().getContentId(), metadata.getContentId());
        assertEquals(requestAndMedata.getMetadata().getContentType(), metadata.getContentType());
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.TITLE_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD));
        assertEquals(requestAndMedata.getMetadata().getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD));
        
    }

}
