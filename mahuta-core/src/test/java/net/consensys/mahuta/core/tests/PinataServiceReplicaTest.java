package net.consensys.mahuta.core.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
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
import net.consensys.mahuta.core.service.pinning.ipfs.PinataPinningService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;

public class PinataServiceReplicaTest extends MahutaTestAbstract {

    private static IndexingRequestUtils indexingRequestUtils;

    private static final String WIREMOCK_HOST = "localhost";
    private static final Integer WIREMOCK_PORT = 9523;
    private static WireMockServer pinataMHTTPMock = new WireMockServer(WIREMOCK_PORT);
   
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        
        pinataMHTTPMock.stubFor(get(urlPathMatching("/data/testAuthentication")).willReturn(okJson("{\"message\":\"Congratulations! You are communicating with the Pinata API!\"}"))); 
        pinataMHTTPMock.stubFor(post(urlPathMatching("/pinning/pinHashToIPFS")).willReturn(okJson("{\"IpfsHash\":\"Qmaisz6NMhDB51cCvNWa1GMS7LU1pAxdF4Ld6Ft9kZEP2a\",\"PinSize\":\"40\",\"Timestamp\":\"2019-10-05T16:18:35.238Z\"}")));
        pinataMHTTPMock.stubFor(post(urlPathMatching("/pinning/removePinFromIPFS")).willReturn(okJson("{\"ipfs_pin_hash\":\"Qmaisz6NMhDB51cCvNWa1GMS7LU1pAxdF4Ld6Ft9kZEP2a\"}"))); 
        pinataMHTTPMock.stubFor(get(urlPathMatching("/data/pinList*")).willReturn(okJson("{\"count\":0,\"rows\":[]}")));     
        pinataMHTTPMock.start();
        
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();   
        pinataMHTTPMock.stop();
    }
    
    
    public PinataServiceReplicaTest () {
        super(Mockito.mock(IndexingService.class), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
                         .addReplica(PinataPinningService.connect("http://"+WIREMOCK_HOST+":"+WIREMOCK_PORT, "apiKey", "secret", null))
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
        
        pinataMHTTPMock.stubFor(get(urlPathMatching("/data/pinList*")).willReturn(okJson("{\"count\":1,\"rows\":[{\"id\":\"21a80e28-d144-438b-898a-1293519c9062\",\"ipfs_pin_hash\":\""+builderAndResponse.getResponse().getContentId()+"\"}]}"))); 
        super.index(builderAndResponse);
    }

    @Test
    public void indexCid() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();
        mockIndex(builderAndResponse);

        pinataMHTTPMock.stubFor(get(urlPathMatching("/data/pinList*")).willReturn(okJson("{\"count\":1,\"rows\":[{\"id\":\"21a80e28-d144-438b-898a-1293519c9062\",\"ipfs_pin_hash\":\""+builderAndResponse.getResponse().getContentId()+"\"}]}")));
        super.index(builderAndResponse);
    }
    
    @Test
    public void deindex() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();

        mockIndex(builderAndResponse);
        mockGetDocument(builderAndResponse);

        pinataMHTTPMock.stubFor(get(urlPathMatching("/data/pinList*")).willReturn(okJson("{\"count\":0,\"rows\":[]}"))); 
        super.deindex(builderAndResponse);
    }
    
}
