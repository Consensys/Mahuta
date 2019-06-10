package net.consensys.mahuta.core.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
import net.consensys.mahuta.core.service.DefaultMahutaService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.FileTestUtils;
import net.consensys.mahuta.core.test.utils.FileTestUtils.FileInfo;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class DefaultMahutaTest extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    public DefaultMahutaTest () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
        indexingRequestUtils = new IndexingRequestUtils(new DefaultMahutaService(storageService, indexingService), 
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
    public void indexStringIndexingContent() throws Exception {

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(true);
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
    
    @Test
    public void prepareCIDndexing() {
        String indexName = mockNeat.strings().size(20).get();
        String indexDocId = mockNeat.strings().size(20).get();
        FileInfo file = FileTestUtils.newRandomPlainText(indexingRequestUtils.getIpfs());
        
        IndexingResponse response = mahuta.prepareCIDndexing(indexName, file.getCid()).indexDocId(indexDocId).execute();
        assertEquals(file.getCid(), response.getContentId());
    }
    
    @Test
    public void prepareStringIndexing() {
        String indexName = mockNeat.strings().size(20).get();
        String indexDocId = mockNeat.strings().size(20).get();
        String content = mockNeat.strings().size(20).get();
        FileInfo file = FileTestUtils.newPlainText(indexingRequestUtils.getIpfs(), content);
        
        IndexingResponse response = mahuta.prepareStringIndexing(indexName, content).indexDocId(indexDocId).execute();
        assertEquals(file.getCid(), response.getContentId());
    }
    
    @Test
    public void prepareInputStreamIndexing() {
        String indexName = mockNeat.strings().size(20).get();
        String indexDocId = mockNeat.strings().size(20).get();
        String content = mockNeat.strings().size(20).get();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        FileInfo file = FileTestUtils.newPlainText(indexingRequestUtils.getIpfs(), content);
        
        IndexingResponse response = mahuta.prepareInputStreamIndexing(indexName, is).indexDocId(indexDocId).execute();
        assertEquals(file.getCid(), response.getContentId());
    }
    
    @Test
    public void prepareStorage() {
        String content = mockNeat.strings().size(20).get();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        FileInfo file = FileTestUtils.newPlainText(indexingRequestUtils.getIpfs(), content);
        
        IndexingResponse response = mahuta.prepareStorage(is).execute();
        assertEquals(file.getCid(), response.getContentId());
    }
    
    @Test
    public void prepareUpdateField() {        
        String indexName = mockNeat.strings().size(20).get();
    BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

    mockIndex(builderAndResponse);
    builderAndResponse.getResponse().getIndexFields().put(IndexingRequestUtils.AUTHOR_FIELD, "test");
    mockGetDocument(builderAndResponse);

    super.updateField(builderAndResponse, IndexingRequestUtils.AUTHOR_FIELD, "test");
    }

    
}
