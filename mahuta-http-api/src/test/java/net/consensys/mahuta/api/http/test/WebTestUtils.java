package net.consensys.mahuta.api.http.test;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.andreinc.mockneat.MockNeat;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;

public class WebTestUtils {

    protected static final MockNeat mockNeat = MockNeat.threadLocal();
    protected static final ObjectMapper mapper = new ObjectMapper();

    
    @Autowired
    protected WebApplicationContext webApplicationContext;
    
    protected MockMvc mockMvc;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
        
        System.setProperty("MAHUTA_IPFS_HOST", ContainerUtils.getHost("ipfs"));
        System.setProperty("MAHUTA_IPFS_PORT", ContainerUtils.getPort("ipfs").toString());
        System.setProperty("MAHUTA_ELASTICSEARCH_HOST", ContainerUtils.getHost("elasticsearch"));
        System.setProperty("MAHUTA_ELASTICSEARCH_PORT", ContainerUtils.getPort("elasticsearch").toString());
        System.setProperty("MAHUTA_ELASTICSEARCH_CLUSTERNAME", ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }

    @Before
    public void setupWeb() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

}
