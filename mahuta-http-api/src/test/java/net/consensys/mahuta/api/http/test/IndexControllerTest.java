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
import net.consensys.mahuta.core.domain.indexing.AbstractIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.utils.BytesUtils;

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
        indexingRequestUtils = new IndexingRequestUtils(null, new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")), true);
    }
    
    @Test
    public void indexFile() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomInputStreamIndexingRequest();
        InputStreamIndexingRequest request = (InputStreamIndexingRequest) builderAndResponse.getBuilder().getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName()).contentType(MediaType.APPLICATION_JSON).content(BytesUtils.readFile("index_mapping.json")))
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
        IndexingResponse result = mapper.readValue(response.getResponse().getContentAsString(), IndexingResponse.class);
        MahutaTestAbstract.validateMetadata(builderAndResponse, result);
    }
    
    @Test
    public void indexCid() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest();
        CIDIndexingRequest request = (CIDIndexingRequest) builderAndResponse.getBuilder().getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName()).contentType(MediaType.APPLICATION_JSON).content(BytesUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());

        MvcResult response = mockMvc.perform(post("/index/cid").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        // Validate
        IndexingResponse result = mapper.readValue(response.getResponse().getContentAsString(), IndexingResponse.class);
        MahutaTestAbstract.validateMetadata(builderAndResponse, result);
    }
    

    @Test
    public void indexString() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();
        StringIndexingRequest request = (StringIndexingRequest) builderAndResponse.getBuilder().getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName()).contentType(MediaType.APPLICATION_JSON).content(BytesUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());

        MvcResult response = mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        // Validate
        IndexingResponse result = mapper.readValue(response.getResponse().getContentAsString(), IndexingResponse.class);
        MahutaTestAbstract.validateMetadata(builderAndResponse, result);
    }
    

    @Test
    public void indexStringNoIndex() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();
        StringIndexingRequest request = (StringIndexingRequest) builderAndResponse.getBuilder().getRequest();
        
        mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
