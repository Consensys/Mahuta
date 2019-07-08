package net.consensys.mahuta.api.http.test;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;

import net.andreinc.mockneat.MockNeat;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;

public class WebTestReplicaUtils {

    protected static final MockNeat mockNeat = MockNeat.threadLocal();
    protected static final ObjectMapper mapper = new ObjectMapper();
    
    private static final String WIREMOCK_HOST = "localhost";
    private static final Integer WIREMOCK_PORT = 9523;
    private static WireMockServer ipfsclusterMHTTPMock = new WireMockServer(WIREMOCK_PORT);

    
    @Autowired
    protected WebApplicationContext webApplicationContext;
    
    protected MockMvc mockMvc;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("ipfs-replica", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);

        ipfsclusterMHTTPMock.stubFor(get(urlPathMatching("/id")).willReturn(okJson("{\"hello\":\"world\"}"))); 
        ipfsclusterMHTTPMock.stubFor(post(urlPathMatching("/pins/.*")).willReturn(okJson("{\"hello\":\"world\"}"))); 
        ipfsclusterMHTTPMock.stubFor(delete(urlPathMatching("/pins/.*")).willReturn(okJson("{\"hello\":\"world\"}")));  
        ipfsclusterMHTTPMock.start();
        
        
        
        System.setProperty("MAHUTA_IPFS_HOST", ContainerUtils.getHost("ipfs"));
        System.setProperty("MAHUTA_IPFS_PORT", ContainerUtils.getPort("ipfs").toString());
        System.setProperty("MAHUTA_IPFS_REPLICAIPFS_0_HOST", ContainerUtils.getHost("ipfs-replica"));
        System.setProperty("MAHUTA_IPFS_REPLICAIPFS_0_PORT", ContainerUtils.getPort("ipfs-replica").toString());
        System.setProperty("MAHUTA_IPFS_REPLICAIPFSCLUSTER_0_HOST", WIREMOCK_HOST);
        System.setProperty("MAHUTA_IPFS_REPLICAIPFSCLUSTER_0_PORT", WIREMOCK_PORT.toString());
        System.setProperty("MAHUTA_ELASTICSEARCH_HOST", ContainerUtils.getHost("elasticsearch"));
        System.setProperty("MAHUTA_ELASTICSEARCH_PORT", ContainerUtils.getPort("elasticsearch").toString());
        System.setProperty("MAHUTA_ELASTICSEARCH_CLUSTERNAME", ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
        ipfsclusterMHTTPMock.stop();
    }

    @Before
    public void setupWeb() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

}
