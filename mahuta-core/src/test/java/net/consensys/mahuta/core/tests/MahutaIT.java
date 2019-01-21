package net.consensys.mahuta.core.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
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
import net.consensys.mahuta.core.test.utils.ConstantUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.utils.FileUtils;

public class MahutaIT extends MahutaTestAbstract {
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    public MahutaIT () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
    }
    
    @Test
    public void indexInputStream() throws Exception {
        indexDocId = mockNeat.strings().get();
        when(indexingService.index(anyString(), anyString(), anyString(), anyString(), any(Map.class))).thenReturn(indexDocId);
        super.indexInputStream();
    }
    
    @Test
    public void indexString() throws Exception {
        indexDocId = mockNeat.strings().get();
        when(indexingService.index(anyString(), anyString(), anyString(), anyString(), any(Map.class))).thenReturn(indexDocId);
        
        super.indexString();
    }
    
    @Test
    public void indexCid() throws Exception {
        indexDocId = mockNeat.strings().get();
        when(indexingService.index(anyString(), anyString(), anyString(), anyString(), any(Map.class))).thenReturn(indexDocId);
        
        super.indexCid();
    }
    
    @Test
    public void deindex() throws Exception {
        indexName = mockNeat.strings().get();
        indexDocId = mockNeat.strings().get();
        contentId = ConstantUtils.TEXT_SAMPLE_HASH;
        contentType = ConstantUtils.TEXT_SAMPLE_TYPE;
        
        when(indexingService.index(anyString(), anyString(), anyString(), anyString(), any(Map.class))).thenReturn(indexDocId);
        when(indexingService.getDocument(anyString(), eq(indexDocId))).thenReturn(Metadata.of(indexName, indexDocId, contentId, null, ImmutableMap.of()));
        
        super.deindex();
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
//    
//    @Test
//    public void getByHash() {
//        String index = "test-index";
//        String id = "id";
//        String hash = FILE_HASH;
//        String type = FILE_TYPE;
//        Map<String, Object> fields = ImmutableMap.of("attribute1", "val1");
//
//        IndexingService indexing = Mockito.mock(IndexingService.class);
//        when(indexing.searchDocuments(eq(index), any(Query.class), any(PageRequest.class)))
//            .thenReturn(Page.of(PageRequest.of(0, 1), Arrays.asList(Metadata.of(index, id, hash, type, fields)), 1));
//
//        Mahuta mahuta = new MahutaFactory()
//                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
//                .configureIndexer(indexing)
//                .build();
//
//        ////////////////////////
//        mahuta.index(FileUtils.readFileInputString(FILE_PATH), index, id, type, fields);
//        MetadataAndPayload result = mahuta.getByHash(index, hash);
//        ///////////////////////
//        
//        assertEquals(index, result.getMetadata().getIndexName());
//        assertEquals(id, result.getMetadata().getIndexDocId());
//        assertEquals(hash, result.getMetadata().getContentId());
//        assertEquals(type, result.getMetadata().getContentType()); 
//        assertEquals(fields.get("attribute1"), result.getMetadata().getIndexFields().get("attribute1"));
//        assertEquals(FileUtils.readFile(FILE_PATH).length, ( (ByteArrayOutputStream) result.getPayload()).size());
//        
//    }
//    
//    @Test
//    public void searchAll() {
//        int page = 0;
//        int size = 20;
//        String index = "test-index";
//        String id1 = "id1";
//        String hash1 = FILE_HASH;
//        String id2 = "id2";
//        String hash2 = FILE_HASH2;
//        String type = FILE_TYPE;
//        Map<String, Object> fields1 = ImmutableMap.of("attribute1", 1);
//        Map<String, Object> fields2 = ImmutableMap.of("attribute1", 2);
//
//        IndexingService indexing = Mockito.mock(IndexingService.class);
//        when(indexing.searchDocuments(eq(index), any(Query.class), any(PageRequest.class)))
//            .thenReturn(Page.of(PageRequest.of(page, size), Arrays.asList(
//                    Metadata.of(index, id1, hash1, type, fields1),
//                    Metadata.of(index, id2, hash2, type, fields2)), 
//                 2));
//
//        Mahuta mahuta = new MahutaFactory()
//                .configureStorage(IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()))
//                .configureIndexer(indexing)
//                .build();
//
//        ////////////////////////
//        mahuta.index(FileUtils.readFileInputString(FILE_PATH), index, id1, type, fields1);
//        mahuta.index(FileUtils.readFileInputString(FILE_PATH2), index, id2, type, fields2);
//        Page<MetadataAndPayload> result = mahuta.searchAndFetch(index);
//        ///////////////////////
//        
//        assertEquals(Integer.valueOf(2), result.getTotalElements());
//        
//        assertEquals(Integer.valueOf(page), result.getPageRequest().getPage());
//        assertEquals(Integer.valueOf(size), result.getPageRequest().getSize());
//        
//        assertEquals(index, result.getContent().get(0).getMetadata().getIndexName());
//        assertEquals(id1, result.getContent().get(0).getMetadata().getIndexDocId());
//        assertEquals(hash1, result.getContent().get(0).getMetadata().getContentId());
//        assertEquals(type, result.getContent().get(0).getMetadata().getContentType()); 
//        assertEquals(fields1.get("attribute1"), result.getContent().get(0).getMetadata().getIndexFields().get("attribute1"));
//        assertEquals(FileUtils.readFile(FILE_PATH).length, ( (ByteArrayOutputStream) result.getContent().get(0).getPayload()).size());
//        
//        assertEquals(index, result.getContent().get(1).getMetadata().getIndexName());
//        assertEquals(id2, result.getContent().get(1).getMetadata().getIndexDocId());
//        assertEquals(hash2, result.getContent().get(1).getMetadata().getContentId());
//        assertEquals(type, result.getContent().get(1).getMetadata().getContentType()); 
//        assertEquals(fields2.get("attribute1"), result.getContent().get(1).getMetadata().getIndexFields().get("attribute1"));
//        assertEquals(FileUtils.readFile(FILE_PATH2).length, ( (ByteArrayOutputStream) result.getContent().get(1).getPayload()).size());
//        
//    }
    
}
