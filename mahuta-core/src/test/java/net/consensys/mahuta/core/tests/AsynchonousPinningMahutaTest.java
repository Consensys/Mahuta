package net.consensys.mahuta.core.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.service.AsynchonousPinningMahutaService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class AsynchonousPinningMahutaTest extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    public AsynchonousPinningMahutaTest () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
        indexingRequestUtils = new IndexingRequestUtils(new AsynchonousPinningMahutaService(storageService, indexingService, 1000), 
                new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
    }
    
    @Test
    public void checkScheduler() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();
        mockIndex(builderAndResponse);
        mockGetIndexes(builderAndResponse.getBuilder().getRequest().getIndexName());
        
        super.index(builderAndResponse);
        
        verify(indexingService, atLeast(1)).getIndexes();
        verify(indexingService, atLeast(1)).searchDocuments(
                eq(builderAndResponse.getBuilder().getRequest().getIndexName()), 
                any(Query.class), 
                any(PageRequest.class));
        
    }

    
}
