package net.consensys.mahuta.test.endpoint;
//package net.consensys.tools.ipfs.ipfsstore.test.endpoint;
//
//import org.json.JSONException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.skyscreamer.jsonassert.JSONAssert;
//import org.springframework.boot.context.embedded.LocalServerPort;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import net.consensys.tools.ipfs.ipfsstore.Application;
//import net.consensys.tools.ipfs.ipfsstore.service.StoreService;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class StoreControllerTest {
//
//    
//    @MockBean
//    private StoreService storeService;
//    
//    TestRestTemplate restTemplate = new TestRestTemplate();
//
//    HttpHeaders headers = new HttpHeaders();
//
//    @Test
//    public void testRetrieveStudentCourse() throws JSONException {
//
//        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                createURLWithPort("${api.store.uri}"),
//                HttpMethod.GET, entity, String.class);
//
//        String expected = "{\n" + 
//                "    \"hash\": \"QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o\"\n" + 
//                "}";
//
//        JSONAssert.assertEquals(expected, response.getBody(), false);
//    }   
//    
//    
//
//
//    private String createURLWithPort(String uri) {
//        return "http://localhost:${local.server.port}" + uri;
//    }
//    
//    
//}
