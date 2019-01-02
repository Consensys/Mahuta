/**
 * 
 */
package net.consensys.mahuta.client.java.test.integration;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.Wait;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.client.java.MahutaClient;
import net.consensys.mahuta.client.java.model.IdAndHash;
import net.consensys.mahuta.client.java.model.MetadataAndPayload;
import net.consensys.mahuta.exception.MahutaException;
import net.consensys.mahuta.exception.NotFoundException;
import net.consensys.mahuta.exception.TimeoutException;

@Slf4j
public class IntegrationTest {
	
	@ClassRule
	public static DockerComposeContainer environment =
	    new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
	            .withExposedService("elasticsearch_1", 9300)
	            .withExposedService("ipfs_1", 5001)
	            .withExposedService("mahuta_1", 8040, Wait.forHttp("/mahuta/query/search").forStatusCode(200));
	
	
    private static final String endpoint = "http://localhost:8040";
    private static final String indexName = "test";
    
    private MahutaClient mahuta;
    
    @Before
    public void setup() {
        mahuta = new MahutaClient(endpoint, indexName);
    }
    
    /* ********************************** *
     * SEARCH BY HASH
     * ********************************** */
    
    @Test 
    public void storeAndSearchByHash() throws MahutaException {
        String fileName = "pdf-sample.pdf";
        String expectedHash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String contentType = "application/pdf";
        
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        IdAndHash idAndHash = mahuta.index(is, indexName, null, contentType);
        assertEquals(expectedHash, idAndHash.getHash());

        /////////////
        MetadataAndPayload result = mahuta.getByHash(indexName, idAndHash.getHash());
        /////////////
        
        assertEquals(expectedHash, result.getMetadata().getHash());
        assertEquals(contentType, result.getMetadata().getContentType());
    }
    

    /* ********************************** *
     * REMOVE
     * ********************************** */
    
    @Test
    public void storeAndRemove() throws MahutaException {
        String content = RandomStringUtils.random(1000, true, true);
        IdAndHash idAndHash = mahuta.index(new ByteArrayInputStream(content.getBytes()), indexName, null, "txt/txt");
        
        /////////////
        mahuta.remove(indexName, idAndHash.getId());
        /////////////
    }
    
    @Test(expected=NotFoundException.class)
    public void storeAndRemoveAndSearchByHash() throws MahutaException {
        String content = RandomStringUtils.random(1000, true, true);
        IdAndHash idAndHash = mahuta.index(new ByteArrayInputStream(content.getBytes()), indexName, null, "txt/txt");
        
        /////////////
        mahuta.remove(indexName, idAndHash.getId());
        /////////////

        // Should throw a NotFoundException
        mahuta.getByHash(indexName, idAndHash.getHash());
    }
    
    @Test(expected=NotFoundException.class)
    public void storeAndRemoveAndSearchById() throws MahutaException {
        String content = RandomStringUtils.random(1000, true, true);
        IdAndHash idAndHash = mahuta.index(new ByteArrayInputStream(content.getBytes()), indexName, null, "txt/txt");
        
        /////////////
        mahuta.remove(indexName, idAndHash.getId());
        /////////////

        // Should throw a NotFoundException
        mahuta.getById(indexName, idAndHash.getId());
    }
    
    
    @Test(expected=NotFoundException.class)
    public void storeAndRemoveNotFound() throws MahutaException {

        /////////////
        mahuta.remove(indexName, "sdfdsfsdf");
        /////////////
    }
    
    @Test(expected=TimeoutException.class)
    public void storeAndRemoveAndFetch() throws MahutaException, IOException, InterruptedException {
        String content = RandomStringUtils.random(1000, true, true);
        IdAndHash idAndHash = mahuta.index(new ByteArrayInputStream(content.getBytes()), indexName, null, "txt/txt");

        /////////////
        mahuta.remove(indexName, idAndHash.getId());
        /////////////
        
        // Run Garbage collector
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("ipfs repo gc");
        int result = pr.waitFor();
        assertEquals(0, result);
        
        // Should throw a TimeoutException
        mahuta.get(indexName, idAndHash.getHash());
    }
    
}
