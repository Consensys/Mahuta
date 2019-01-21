package net.consensys.mahuta.core.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Map;

import net.andreinc.mockneat.MockNeat;
import net.andreinc.mockneat.types.enums.StringType;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.utils.FileUtils;

public abstract class MahutaTestAbstract {
    
    protected static final MockNeat mockNeat = MockNeat.threadLocal();

    protected final IndexingService indexingService;
    protected final StorageService storageService;
    protected final Mahuta mahuta;
    
    protected String indexName;
    protected String indexDocId;
    protected String contentId;
    protected String contentType;
    protected Map<String, Object> indexFields;
    
    
    public MahutaTestAbstract(IndexingService indexingService, StorageService storageService) {
        this.indexingService = indexingService;
        this.storageService = storageService;
        this.mahuta = new MahutaFactory().configureStorage(storageService).configureIndexer(indexingService).build();
    }
    
    protected void indexInputStream() throws Exception {

        indexName = mockNeat.strings().size(20).type(StringType.ALPHA_NUMERIC).get();
        IndexingRequest request = IndexingRequestUtils.generateRandomInputStreamIndexingRequest(
                indexName, FileUtils.readFileInputString(ConstantUtils.FILE_PATH), indexDocId);
        
        ////////////////////////
        Metadata metadata = mahuta.index(request);
        ///////////////////////
        
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(indexDocId, metadata.getIndexDocId());
        assertEquals(ConstantUtils.FILE_HASH, metadata.getContentId());
        assertEquals(ConstantUtils.FILE_TYPE, metadata.getContentType());
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD));
    }
    
    protected void indexString() throws Exception {

        indexName = mockNeat.strings().size(20).type(StringType.ALPHA_NUMERIC).get();
        IndexingRequest request = IndexingRequestUtils.generateRandomStringIndexingRequest(indexName, indexDocId);
        
        ////////////////////////
        Metadata metadata = mahuta.index(request);
        ///////////////////////
        
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(indexDocId, metadata.getIndexDocId());
        assertNotNull(metadata.getContentId());
        assertEquals(ConstantUtils.TEXT_SAMPLE_TYPE, metadata.getContentType());
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD));
    }
    
    protected void indexCid() throws Exception {

        indexName = mockNeat.strings().size(20).type(StringType.ALPHA_NUMERIC).get();
        IndexingRequest request = IndexingRequestUtils.generateRandomCIDIndexingRequest(indexName, ConstantUtils.TEXT_SAMPLE_HASH, indexDocId);
        
        ////////////////////////
        Metadata metadata = mahuta.index(request);
        ///////////////////////
        
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(indexDocId, metadata.getIndexDocId());
        assertEquals(ConstantUtils.TEXT_SAMPLE_HASH, metadata.getContentId());
        assertEquals(ConstantUtils.TEXT_SAMPLE_TYPE, metadata.getContentType());
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.AUTHOR_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.TITLE_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.IS_PUBLISHED_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.DATE_CREATED_FIELD));
        assertEquals(request.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD), metadata.getIndexFields().get(IndexingRequestUtils.VIEWS_FIELD));
    }
    
    protected void deindex() throws Exception {
        mahuta.index(contentId, indexName);
        mahuta.deindex(indexName, indexDocId);
    }

}
