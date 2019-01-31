package net.consensys.mahuta.api.http.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.indexing.AbstractIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IndexingRequestAndMetadata;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.utils.FileUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IndexControllerTest extends WebTestUtils {
    
    @Configuration
    @EnableWebMvc
    @ComponentScan( basePackages = { "net.consensys.mahuta.api.http" } )
    static class ContextConfiguration { }
    

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void init() throws IOException, InterruptedException {
        indexingRequestUtils = new IndexingRequestUtils(new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")), true);
    }
    
    @Test
    public void indexFile() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomInputStreamIndexingRequest();
        InputStreamIndexingRequest request = (InputStreamIndexingRequest) requestAndMetadata.getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName()).contentType(MediaType.APPLICATION_JSON).content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test_file", request.getContentType(), IOUtils.toByteArray(request.getContent()));
        request.setContent(null);
        MockMultipartFile mockMultipartRequest = new MockMultipartFile("request", "", "application/json", mapper.writeValueAsBytes((AbstractIndexingRequest)request));

        MvcResult response = mockMvc.perform(multipart("/index/file")
                .file(mockMultipartFile)
                .file(mockMultipartRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        // Validate
        Metadata result = mapper.readValue(response.getResponse().getContentAsString(), Metadata.class);
        MahutaTestAbstract.validateMetadata(requestAndMetadata, result);
    }
    
    @Test
    public void indexCid() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();
        CIDIndexingRequest request = (CIDIndexingRequest) requestAndMetadata.getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName()).contentType(MediaType.APPLICATION_JSON).content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());

        MvcResult response = mockMvc.perform(post("/index/cid").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        // Validate
        Metadata result = mapper.readValue(response.getResponse().getContentAsString(), Metadata.class);
        MahutaTestAbstract.validateMetadata(requestAndMetadata, result);
    }
    

    @Test
    public void indexString() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest();
        StringIndexingRequest request = (StringIndexingRequest) requestAndMetadata.getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName()).contentType(MediaType.APPLICATION_JSON).content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());

        MvcResult response = mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        // Validate
        Metadata result = mapper.readValue(response.getResponse().getContentAsString(), Metadata.class);
        MahutaTestAbstract.validateMetadata(requestAndMetadata, result);
    }
}
