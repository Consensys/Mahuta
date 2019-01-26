//package net.consensys.mahuta.core.api.http.test;
//
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//import net.consensys.mahuta.core.test.utils.ContainerUtils;
//import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class ConfigTest {
//    
//    @BeforeClass
//    public static void startContainers() throws IOException {
//        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
//        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
//        
//        System.setProperty("MAHUTA_IPFS_HOST", ContainerUtils.getHost("ipfs"));
//        System.setProperty("MAHUTA_IPFS_PORT", ContainerUtils.getPort("ipfs").toString());
//        System.setProperty("MAHUTA_ELASTICSEARCH_HOST", ContainerUtils.getHost("elasticsearch"));
//        System.setProperty("MAHUTA_ELASTICSEARCH_PORT", ContainerUtils.getPort("elasticsearch").toString());
//        
//    }
//    
//    @AfterClass
//    public static void stopContainers() {
//        ContainerUtils.stopAll();
//    }
//    
//    /* *************************************
//     * CONTEXT LOADER
//     */
//    @Configuration
//    @EnableWebMvc
//    @ComponentScan( basePackages = { "net.consensys.mahuta.core.api.http" } )
//    static class ContextConfiguration { }
//    
//    
//    @Test
//    public void test() {
//        assertTrue(true);
//    }
//}
