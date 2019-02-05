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
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.service.MahutaServiceImpl;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class MahutaTest extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("ipfs-replica1", ContainerType.IPFS);
        ContainerUtils.startContainer("ipfs-replica2", ContainerType.IPFS);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    public MahutaTest () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
                         .addReplica(IPFSService.connect(ContainerUtils.getHost("ipfs-replica1"), ContainerUtils.getPort("ipfs-replica1")))
                         .addReplica(IPFSService.connect(ContainerUtils.getHost("ipfs-replica2"), ContainerUtils.getPort("ipfs-replica2")))
        );
        indexingRequestUtils = new IndexingRequestUtils(new MahutaServiceImpl(storageService, indexingService), 
                new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
    }
    
    @Test
    public void createIndex() throws Exception {
        String indexName = mockNeat.strings().get();
        mockGetIndexes(indexName);
        
        super.creatIndex(indexName);
    }
    
    @Test
    public void indexInputStream() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomInputStreamIndexingRequest();
        mockIndex(builderAndResponse);
        
        super.index(builderAndResponse);
    }
    
    @Test
    public void indexString() throws Exception {

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();
        mockIndex(builderAndResponse);
        
        super.index(builderAndResponse);
    }
    
    @Test
    public void indexCid() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();
        mockIndex(builderAndResponse);
        
        super.index(builderAndResponse);
    }
    
    @Test
    public void deindex() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();

        mockIndex(builderAndResponse);
        mockGetDocument(builderAndResponse);
        
        super.deindex(builderAndResponse);
    }
    
    @Test
    public void getById() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();
        mockIndex(builderAndResponse);
        mockGetDocument(builderAndResponse);
        
        super.getById(builderAndResponse);
    }
    
    @Test
    public void getByHash() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();

        mockIndex(builderAndResponse);
        
        when(indexingService.searchDocuments(
                eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                any(Query.class),
                any(PageRequest.class)))
        .thenReturn(Page.of(builderAndResponse.getResponse()));
        
        super.getByHash(builderAndResponse);
    }
    
    @Test
    public void searchAll() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

        mockIndex(builderAndResponse1);
        mockIndex(builderAndResponse2);
        mockIndex(builderAndResponse3);
        mockSearchDocuments(indexName, 3, null, builderAndResponse1, builderAndResponse2, builderAndResponse3);
        
        super.searchAll(Arrays.asList(builderAndResponse1, builderAndResponse2, builderAndResponse3), 3);
    }
    
    
    /////////////////////////////////////////

    private void mockGetIndexes(String indexName) {
        when(indexingService.getIndexes())
        .thenReturn(Arrays.asList(mockNeat.strings().get(), mockNeat.strings().get(), indexName));
    } 
    
    private void mockIndex(BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse) {
        when(indexingService.index(
                eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                eq(builderAndResponse.getBuilder().getRequest().getIndexDocId()), 
                eq(builderAndResponse.getResponse().getContentId()), 
                eq(builderAndResponse.getBuilder().getRequest().getContentType()), 
                eq(builderAndResponse.getBuilder().getRequest().getIndexFields())))
        .thenReturn(builderAndResponse.getResponse().getIndexDocId());
    }  
    
    private void mockGetDocument(BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse) {
        when(indexingService.getDocument(
                eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                eq(builderAndResponse.getBuilder().getRequest().getIndexDocId())))
        .thenReturn(builderAndResponse.getResponse());
    } 
    
    private void mockSearchDocuments(String indexName, Integer totalNo, Query query,  BuilderAndResponse<IndexingRequest, IndexingResponse>... builderAndResponses) {
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
