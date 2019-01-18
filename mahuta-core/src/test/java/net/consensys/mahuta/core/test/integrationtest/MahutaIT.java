package net.consensys.mahuta.core.test.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.IntegrationTestUtils;
import net.consensys.mahuta.core.utils.FileUtils;

public class MahutaIT extends IntegrationTestUtils {

    @Test
    public void build() throws Exception {
        IndexingService indexing = Mockito.mock(IndexingService.class);

        ////////////////////////
        new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();
        ///////////////////////
    }
    
    @Test
    public void indexFile() throws Exception {
        String index = "test-index";
        String id = "id";
        String hash = FILE_HASH;
        String type = FILE_TYPE;
        Map<String, Object> fields = ImmutableMap.of();

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.index(eq(index), eq(null), eq(hash), eq(null), eq(fields))).thenReturn(id);
        
        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();
        
        ////////////////////////
        Metadata metadata = mahuta.index(FileUtils.readFileInputString(FILE_PATH), index);
        ///////////////////////
        
        assertEquals(index, metadata.getIndexName());
        assertEquals(id, metadata.getIndexDocId());
        assertEquals(hash, metadata.getContentId());
        assertNull(metadata.getContentType()); // Doesn't work with PDF TODO investigate this
        assertEquals(ImmutableMap.of(), metadata.getIndexFields());
    }
    
    @Test
    public void indexFile2() throws Exception {
        String index = "test-index";
        String id = "id";
        String hash = FILE_HASH;
        String type = FILE_TYPE;
        Map<String, Object> fields = ImmutableMap.of("attribute1", "val1");

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.index(eq(index), eq(id), eq(hash), eq(type), eq(fields))).thenReturn(id);
        
        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();
        
        ////////////////////////
        Metadata metadata = mahuta.index(FileUtils.readFileInputString(FILE_PATH), index, id, type, fields);
        ///////////////////////
        
        assertEquals(index, metadata.getIndexName());
        assertEquals(id, metadata.getIndexDocId());
        assertEquals(hash, metadata.getContentId());
        assertEquals(type, metadata.getContentType()); 
        assertEquals(fields.get("attribute1"), metadata.getIndexFields().get("attribute1"));
    }
    
    @Test
    public void indexCid() throws Exception {
        String index = "test-index";
        String cid = TEXT_SAMPLE_HASH;
        String id = "sdasd";

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.index(eq(index), eq(null), eq(cid), eq(null), eq(ImmutableMap.of()))).thenReturn(id);
        
        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();
        
        ////////////////////////
        Metadata metadata = mahuta.index(cid, index);
        ///////////////////////
        
        assertEquals(index, metadata.getIndexName());
        assertEquals(id, metadata.getIndexDocId());
        assertEquals(cid, metadata.getContentId());
    }
    
    @Test
    public void deindex() throws Exception {
        String index = "test-index";
        String cid = TEXT_SAMPLE_HASH;
        String id = "sdasd";

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.index(eq(index), eq(null), eq(cid), eq(null), eq(ImmutableMap.of()))).thenReturn(id);
        when(indexing.getDocument(eq(index), eq(id))).thenReturn(Metadata.of(index, id, cid, null, ImmutableMap.of()));
        
        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();
        
        ////////////////////////
        mahuta.index(cid, index);
        mahuta.deindex(index, id);
        ///////////////////////
    }
    
    @Test(expected=TechnicalException.class)
    public void deindexUnpinnedFile() throws Exception {
        String index = "test-index";
        String cid = CID;
        String id = "sdasd";

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.index(eq(index), eq(null), eq(cid), eq(null), eq(ImmutableMap.of()))).thenReturn(id);
        when(indexing.getDocument(eq(index), eq(id))).thenReturn(Metadata.of(index, id, cid, null, ImmutableMap.of()));
        
        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();
        
        ////////////////////////
        mahuta.deindex(index, id);
        ///////////////////////
    }
    
    @Test
    public void getById() {
        String index = "test-index";
        String id = "id";
        String hash = FILE_HASH;
        String type = FILE_TYPE;
        Map<String, Object> fields = ImmutableMap.of("attribute1", "val1");

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.getDocument(eq(index), eq(id))).thenReturn(Metadata.of(index, id, hash, type, fields));

        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();

        ////////////////////////
        mahuta.index(FileUtils.readFileInputString(FILE_PATH), index, id, type, fields);
        MetadataAndPayload result = mahuta.getById(index, id);
        ///////////////////////
        
        assertEquals(index, result.getMetadata().getIndexName());
        assertEquals(id, result.getMetadata().getIndexDocId());
        assertEquals(hash, result.getMetadata().getContentId());
        assertEquals(type, result.getMetadata().getContentType()); 
        assertEquals(fields.get("attribute1"), result.getMetadata().getIndexFields().get("attribute1"));
        assertEquals(FileUtils.readFile(FILE_PATH).length, ( (ByteArrayOutputStream) result.getPayload()).size());
        
    }
    
    @Test
    public void getByHash() {
        String index = "test-index";
        String id = "id";
        String hash = FILE_HASH;
        String type = FILE_TYPE;
        Map<String, Object> fields = ImmutableMap.of("attribute1", "val1");

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.searchDocuments(eq(index), any(Query.class), any(PageRequest.class)))
            .thenReturn(Page.of(PageRequest.of(0, 1), Arrays.asList(Metadata.of(index, id, hash, type, fields)), 1));

        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();

        ////////////////////////
        mahuta.index(FileUtils.readFileInputString(FILE_PATH), index, id, type, fields);
        MetadataAndPayload result = mahuta.getByHash(index, hash);
        ///////////////////////
        
        assertEquals(index, result.getMetadata().getIndexName());
        assertEquals(id, result.getMetadata().getIndexDocId());
        assertEquals(hash, result.getMetadata().getContentId());
        assertEquals(type, result.getMetadata().getContentType()); 
        assertEquals(fields.get("attribute1"), result.getMetadata().getIndexFields().get("attribute1"));
        assertEquals(FileUtils.readFile(FILE_PATH).length, ( (ByteArrayOutputStream) result.getPayload()).size());
        
    }
    
    @Test
    public void searchAll() {
        int page = 0;
        int size = 20;
        String index = "test-index";
        String id1 = "id1";
        String hash1 = FILE_HASH;
        String id2 = "id2";
        String hash2 = FILE_HASH2;
        String type = FILE_TYPE;
        Map<String, Object> fields1 = ImmutableMap.of("attribute1", 1);
        Map<String, Object> fields2 = ImmutableMap.of("attribute1", 2);

        IndexingService indexing = Mockito.mock(IndexingService.class);
        when(indexing.searchDocuments(eq(index), any(Query.class), any(PageRequest.class)))
            .thenReturn(Page.of(PageRequest.of(page, size), Arrays.asList(
                    Metadata.of(index, id1, hash1, type, fields1),
                    Metadata.of(index, id2, hash2, type, fields2)), 
                 2));

        Mahuta mahuta = new MahutaFactory()
                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
                .configureIndexer(indexing)
                .build();

        ////////////////////////
        mahuta.index(FileUtils.readFileInputString(FILE_PATH), index, id1, type, fields1);
        mahuta.index(FileUtils.readFileInputString(FILE_PATH2), index, id2, type, fields2);
        Page<MetadataAndPayload> result = mahuta.searchAndFetch(index);
        ///////////////////////
        
        assertEquals(Integer.valueOf(2), result.getTotalElements());
        
        assertEquals(Integer.valueOf(page), result.getPageRequest().getPage());
        assertEquals(Integer.valueOf(size), result.getPageRequest().getSize());
        
        assertEquals(index, result.getContent().get(0).getMetadata().getIndexName());
        assertEquals(id1, result.getContent().get(0).getMetadata().getIndexDocId());
        assertEquals(hash1, result.getContent().get(0).getMetadata().getContentId());
        assertEquals(type, result.getContent().get(0).getMetadata().getContentType()); 
        assertEquals(fields1.get("attribute1"), result.getContent().get(0).getMetadata().getIndexFields().get("attribute1"));
        assertEquals(FileUtils.readFile(FILE_PATH).length, ( (ByteArrayOutputStream) result.getContent().get(0).getPayload()).size());
        
        assertEquals(index, result.getContent().get(1).getMetadata().getIndexName());
        assertEquals(id2, result.getContent().get(1).getMetadata().getIndexDocId());
        assertEquals(hash2, result.getContent().get(1).getMetadata().getContentId());
        assertEquals(type, result.getContent().get(1).getMetadata().getContentType()); 
        assertEquals(fields2.get("attribute1"), result.getContent().get(1).getMetadata().getIndexFields().get("attribute1"));
        assertEquals(FileUtils.readFile(FILE_PATH2).length, ( (ByteArrayOutputStream) result.getContent().get(1).getPayload()).size());
        
    }
    
}
