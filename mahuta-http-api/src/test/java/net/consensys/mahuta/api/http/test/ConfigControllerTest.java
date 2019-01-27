package net.consensys.mahuta.api.http.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.type.TypeReference;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ConfigControllerTest extends WebTestUtils {
    
    @Configuration
    @EnableWebMvc
    @ComponentScan( basePackages = { "net.consensys.mahuta.api.http" } )
    static class ContextConfiguration { }
    

    @Test
    public void createIndexNoConfig() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        
        // Create Index
        mockMvc.perform(post("/config/index/" + indexName))
            .andExpect(status().isOk())
            .andDo(print());
        
        // Get all Indexes
        MvcResult response = mockMvc.perform(get("/config/index"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        
        // Validate
        List<String> result = mapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<String>>() {});
        assertTrue(result.stream().filter(i->i.equalsIgnoreCase(indexName)).findAny().isPresent());
    }
    
    @Test
    public void findIndexThatDoesNotExist() throws Exception {
        String indexName = mockNeat.strings().size(20).get();

        // Get all Indexes
        MvcResult response = mockMvc.perform(get("/config/index"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        
        // Validate
        List<String> result = mapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<String>>() {});
        assertFalse(result.stream().filter(i->i.equalsIgnoreCase(indexName)).findAny().isPresent());
    }
}
