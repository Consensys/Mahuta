package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.Response.ResponseStatus;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.AsynchonousPinningMahutaService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.utils.BytesUtils;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class AsynchonousPinningMahutaTest extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;

    private String indexName;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    public AsynchonousPinningMahutaTest () {
        super(ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name")), 
                IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
          );
          indexingRequestUtils = new IndexingRequestUtils(new AsynchonousPinningMahutaService(storageService, indexingService, 1000), 
                  new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
        
    }
    
    @Before
    public void before() {
        indexName = mockNeat.strings().size(20).get();
        indexingService.createIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
    }
    
    @Test
    public void checkScheduler() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        
        ////////////////////////
        IndexingResponse indexingResponse = builderAndResponse.getBuilder().execute();
        assertEquals(ResponseStatus.SUCCESS, indexingResponse.getStatus());
        
        Thread.sleep(2000);
        
        GetResponse getResponse = mahuta.prepareGet()
                .indexName(builderAndResponse.getBuilder().getRequest().getIndexName())
                .indexDocId(builderAndResponse.getBuilder().getRequest().getIndexDocId())
                .loadFile(true)
                .execute();
        assertEquals(ResponseStatus.SUCCESS, getResponse.getStatus());
        ////////////////////////

        assertTrue(getResponse.getMetadata().isPinned());
        
    }

    
}
