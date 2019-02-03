package net.consensys.mahuta.api.http.test;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.utils.FileUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class QueryControllerTest extends WebTestUtils {
    
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
    public void fetch() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();
        StringIndexingRequest request = (StringIndexingRequest) builderAndResponse.getBuilder().getRequest();
        
        // Create Index 
        mockMvc.perform(post("/config/index/" + request.getIndexName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());

        mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MvcResult response = mockMvc.perform(get("/query/fetch/"+builderAndResponse.getResponse().getContentId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(builderAndResponse.getBuilder().getRequest().getContentType()))
                .andReturn();
        
        assertEquals(request.getContent(), response.getResponse().getContentAsString());
    }
    
    @Test
    public void findAll() throws Exception {
        int no = 30;
        String indexName = mockNeat.strings().size(20).get();

        // Create Index 
        mockMvc.perform(post("/config/index/" + indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());
        
        IntStream.range(0, no).forEach(i -> {
            BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
            StringIndexingRequest request = (StringIndexingRequest) builderAndResponse.getBuilder().getRequest();
            
            try {
                mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        

        MvcResult response = mockMvc.perform(post("/query/search/?index="+indexName).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SearchResponse result = mapper.readValue(response.getResponse().getContentAsString(), SearchResponse.class);
        
        assertEquals(no, result.getPage().getTotalElements().intValue());
        assertEquals(2, result.getPage().getTotalPages().intValue());
        assertEquals(20, result.getPage().getElements().size());
    }
    
    @Test
    public void findWithPagination() throws Exception {
        int no = 8;
        String indexName = mockNeat.strings().size(20).get();

        // Create Index 
        mockMvc.perform(post("/config/index/" + indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());
        
        IntStream.range(0, no).forEach(i -> {
            BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
            StringIndexingRequest request = (StringIndexingRequest) builderAndResponse.getBuilder().getRequest();
            
            try {
                mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        

        MvcResult response = mockMvc.perform(post("/query/search/?index="+indexName+"&page=1&size=5").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SearchResponse result = mapper.readValue(response.getResponse().getContentAsString(), SearchResponse.class);
        
        assertEquals(no, result.getPage().getTotalElements().intValue());
        assertEquals(2, result.getPage().getTotalPages().intValue());
        assertEquals(3, result.getPage().getElements().size());
    }
    
    @Test
    public void findWithSort() throws Exception {
        String indexName = mockNeat.strings().size(20).get();

        // Create Index 
        mockMvc.perform(post("/config/index/" + indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, "1", IndexingRequestUtils.VIEWS_FIELD, 1);
        StringIndexingRequest request1 = (StringIndexingRequest) builderAndResponse1.getBuilder().getRequest();
        mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request1)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, "2", IndexingRequestUtils.VIEWS_FIELD, 2);
        StringIndexingRequest request2 = (StringIndexingRequest) builderAndResponse2.getBuilder().getRequest();
        mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request2)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

        

        MvcResult response = mockMvc.perform(post("/query/search/?index="+indexName + "&sort="+ IndexingRequestUtils.VIEWS_FIELD+"&dir=DESC").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SearchResponse result = mapper.readValue(response.getResponse().getContentAsString(), SearchResponse.class);
        
        assertEquals(2, result.getPage().getTotalElements().intValue());
        assertEquals(request2.getIndexDocId(), result.getPage().getElements().get(0).getMetadata().getIndexDocId());
    }
    
    @Test
    public void findWithSearch() throws Exception {
        String indexName = mockNeat.strings().size(20).get();

        // Create Index 
        mockMvc.perform(post("/config/index/" + indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(FileUtils.readFile("index_mapping.json")))
            .andExpect(status().isOk())
            .andDo(print());
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, "1", IndexingRequestUtils.AUTHOR_FIELD, "Gregoire Jeanmart");
        StringIndexingRequest request1 = (StringIndexingRequest) builderAndResponse1.getBuilder().getRequest();
        mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request1)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, "2", IndexingRequestUtils.AUTHOR_FIELD, "Bob Dylan");
        StringIndexingRequest request2 = (StringIndexingRequest) builderAndResponse2.getBuilder().getRequest();
        mockMvc.perform(post("/index").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request2)))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

        

        MvcResult response = mockMvc.perform(post("/query/search/?index="+indexName).contentType(MediaType.APPLICATION_JSON)
                .content("{\n" + 
                        "  \"query\": [\n" + 
                        "    {\n" + 
                        "      \"names\": [\"author\"],\n" + 
                        "      \"operation\": \"FULL_TEXT\",\n" + 
                        "      \"value\": \"Gregoire\"\n" + 
                        "    }\n" + 
                        "  ]\n" + 
                        "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SearchResponse result = mapper.readValue(response.getResponse().getContentAsString(), SearchResponse.class);
        
        assertEquals(1, result.getPage().getTotalElements().intValue());
        assertEquals(request1.getIndexDocId(), result.getPage().getElements().get(0).getMetadata().getIndexDocId());
    }
   
}
