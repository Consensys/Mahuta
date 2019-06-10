package net.consensys.mahuta.core.tests;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.service.DefaultMahutaService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class MahutaTwoIPFSReplicasTest extends MahutaTestAbstract {

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
    
    public  MahutaTwoIPFSReplicasTest () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
                         .addReplica(IPFSService.connect(ContainerUtils.getHost("ipfs-replica1"), ContainerUtils.getPort("ipfs-replica1")))
                         .addReplica(IPFSService.connect(ContainerUtils.getHost("ipfs-replica2"), ContainerUtils.getPort("ipfs-replica2")))
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
    
}
