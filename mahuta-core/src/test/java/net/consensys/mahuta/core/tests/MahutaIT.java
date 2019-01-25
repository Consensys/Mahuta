package net.consensys.mahuta.core.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IndexingRequestAndMetadata;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class MahutaIT extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        indexingRequestUtils = new IndexingRequestUtils(new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
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
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomInputStreamIndexingRequest();
        mockIndex(requestAndMetadata);
        
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexByteArray() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomByteArrayIndexingRequest();
        mockIndex(requestAndMetadata);
        
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexString() throws Exception {

        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest();
        mockIndex(requestAndMetadata);
        
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexCid() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();
        mockIndex(requestAndMetadata);
        
        super.index(requestAndMetadata);
    }
    
    @Test
    public void deindex() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();

        mockIndex(requestAndMetadata);
        
        when(indexingService.getDocument(
                eq(requestAndMetadata.getRequest().getIndexName()), 
                eq(requestAndMetadata.getRequest().getIndexDocId())))
        .thenReturn(requestAndMetadata.getMetadata());
        
        super.deindex(requestAndMetadata);
    }
    
    @Test
    public void getById() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();
        mockIndex(requestAndMetadata);
        mockGetDocument(requestAndMetadata);
        
        super.getById(requestAndMetadata);
    }
    
    @Test
    public void getByHash() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();

        mockIndex(requestAndMetadata);
        
        when(indexingService.searchDocuments(
                eq(requestAndMetadata.getRequest().getIndexName()), 
                any(Query.class),
                any(PageRequest.class)))
        .thenReturn(Page.of(requestAndMetadata.getMetadata()));
        
        super.getByHash(requestAndMetadata);
    }
    
    @Test
    public void searchAll() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

        mockIndex(requestAndMetadata1);
        mockIndex(requestAndMetadata2);
        mockIndex(requestAndMetadata3);
        mockSearchDocuments(indexName, 3, null, requestAndMetadata1, requestAndMetadata2, requestAndMetadata3);
        
        super.searchAll(Arrays.asList(requestAndMetadata1, requestAndMetadata2, requestAndMetadata3), 3);
    }
    
    
    /////////////////////////////////////////

    private void mockIndex(IndexingRequestAndMetadata requestAndMetadata) {
        when(indexingService.index(
                eq(requestAndMetadata.getRequest().getIndexName()), 
                eq(requestAndMetadata.getRequest().getIndexDocId()), 
                eq(requestAndMetadata.getMetadata().getContentId()), 
                eq(requestAndMetadata.getRequest().getContentType()), 
                eq(requestAndMetadata.getMetadata().getIndexFields())))
        .thenReturn(requestAndMetadata.getMetadata().getIndexDocId());
    }  
    
    private void mockGetDocument(IndexingRequestAndMetadata requestAndMetadata) {
        when(indexingService.getDocument(
                eq(requestAndMetadata.getRequest().getIndexName()), 
                eq(requestAndMetadata.getRequest().getIndexDocId())))
        .thenReturn(requestAndMetadata.getMetadata());
    } 
    
    private void mockSearchDocuments(String indexName, Integer totalNo, Query query,  IndexingRequestAndMetadata... requestAndMetadatas) {
        when(indexingService.searchDocuments(
                eq(indexName), 
                eq(query),
                any(PageRequest.class)))
        .thenReturn(Page.of(
                PageRequest.of(), 
                Arrays.asList(requestAndMetadatas).stream().map(r->r.getMetadata()).collect(Collectors.toList()), 
                totalNo));
    }
    
}
