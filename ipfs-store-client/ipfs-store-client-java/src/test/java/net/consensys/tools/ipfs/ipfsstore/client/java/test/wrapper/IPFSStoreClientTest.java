package net.consensys.tools.ipfs.ipfsstore.client.java.test.wrapper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.FileInputStream;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreClientException;
import net.consensys.tools.ipfs.ipfsstore.client.java.service.impl.IPFSStoreClientServiceImpl;



@RunWith(SpringRunner.class)
public class IPFSStoreClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(IPFSStoreClientTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENDPOINT = "http://localhost:8040";
    private static final String INDEX_NAME = "documents";

    private MockRestServiceServer mockServer;
    
    private IPFSStoreClientServiceImpl undertest;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        this.undertest = new IPFSStoreClientServiceImpl(ENDPOINT);

        this.mockServer = MockRestServiceServer.createServer(this.undertest.getWrapper().getClient());
        
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Test
    public void storeTestFilePath() throws Exception {
        
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        
        // MOCK
        String responseStore = 
                "{\n" + 
                "    \"hash\": \""+hash+"\"\n" + 
                "}";
        
        mockServer.expect(requestTo(ENDPOINT+"/ipfs-store/store"))
                .andExpect(header("content-type", containsString("multipart/form-data")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));
        
        // ###########################
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("pdf-sample.pdf").getFile());
        String hashReturned = this.undertest.store(file.getAbsolutePath());
        // ###########################
        
        LOG.info("hashReturned="+hashReturned);
        
        mockServer.verify();
        assertEquals(hash, hashReturned);
    }
    
    @Test
    public void storeTestInputStream() throws Exception {
        
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        
        // MOCK
        String responseStore = 
                "{\n" + 
                "    \"hash\": \""+hash+"\"\n" + 
                "}";
        
        mockServer.expect(requestTo(ENDPOINT+"/ipfs-store/store"))
                .andExpect(header("content-type", containsString("multipart/form-data")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));
        
        // ###########################
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("pdf-sample.pdf").getFile());
        String hashReturned = this.undertest.store(new FileInputStream(file));
        // ###########################
        
        LOG.info("hashReturned="+hashReturned);
        
        mockServer.verify();
        assertEquals(hash, hashReturned);
    }
    
    @Test(expected=IPFSStoreClientException.class)
    public void storeTestWrongfile() throws Exception {
        
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        
        // MOCK
        String responseStore = 
                "{\n" + 
                "    \"hash\": \""+hash+"\"\n" + 
                "}";
        
        mockServer.expect(requestTo(ENDPOINT+"/ipfs-store/store"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));
        
        // ###########################
        this.undertest.store("donotexist.pdf");
        // ###########################
    }
    
    @Test
    public void indexTest() throws Exception {
        
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String id = "ABC";
        
        // MOCK
        String responseIndex = 
                "{\n" + 
                "    \"index\": \""+INDEX_NAME+"\",\n" + 
                "    \"id\": \""+id+"\",\n" + 
                "    \"hash\": \""+hash+"\"\n" + 
                "}";
        
        mockServer.expect(requestTo(ENDPOINT+"/ipfs-store/index"))
        .andExpect(header("content-type", containsString("application/json")))
        .andExpect(jsonPath("index", containsString(INDEX_NAME)))
        .andExpect(jsonPath("hash", containsString(hash)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));
        
        // ###########################
        String idReturned = this.undertest.index(INDEX_NAME, hash);
        // ###########################


        LOG.info("idReturned="+idReturned);
         
        assertEquals(id, idReturned);
    }
    
    
}
