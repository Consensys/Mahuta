package net.consensys.mahuta.core.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.service.DefaultMahutaService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.pinning.ipfs.IPFSClusterPinningService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;

public class MahutaIPFSClusterReplicasTest extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;

    private static final String WIREMOCK_HOST = "localhost";
    private static final Integer WIREMOCK_PORT = 9523;
    private static WireMockServer ipfsclusterMHTTPMock = new WireMockServer(WIREMOCK_PORT);
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        

        ipfsclusterMHTTPMock.stubFor(get(urlPathMatching("/id")).willReturn(okJson("{\"hello\":\"world\"}"))); 
        ipfsclusterMHTTPMock.stubFor(post(urlPathMatching("/pins/.*")).willReturn(okJson("{\"hello\":\"world\"}"))); 
        ipfsclusterMHTTPMock.stubFor(delete(urlPathMatching("/pins/.*")).willReturn(okJson("{\"hello\":\"world\"}")));     
        ipfsclusterMHTTPMock.start();
        
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();   
        ipfsclusterMHTTPMock.stop();
    }
    
    public MahutaIPFSClusterReplicasTest () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
                         .addReplica(IPFSClusterPinningService.connect(WIREMOCK_HOST, WIREMOCK_PORT))
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

        ipfsclusterMHTTPMock.stubFor(get(urlPathMatching("/pins")).willReturn(okJson("{\"pins\": [\""+builderAndResponse.getResponse().getContentId()+"\"]}"))); 
        super.index(builderAndResponse);
    }

    @Test
    public void indexCid() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();
        mockIndex(builderAndResponse);

        ipfsclusterMHTTPMock.stubFor(get(urlPathMatching("/pins")).willReturn(okJson("{\"pins\": [\""+builderAndResponse.getResponse().getContentId()+"\"]}"))); 
        super.index(builderAndResponse);
    }
    
    @Test
    public void deindex() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();

        mockIndex(builderAndResponse);
        mockGetDocument(builderAndResponse);

        ipfsclusterMHTTPMock.stubFor(get(urlPathMatching("/pins")).willReturn(okJson("{\"pins\": []}"))); 
        super.deindex(builderAndResponse);
    }
    
}
