/**
 * 
 */
package net.consensys.mahuta.client.java.test.integration;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.client.java.MahutaClient;
import net.consensys.mahuta.client.java.model.IdAndHash;
import net.consensys.mahuta.client.java.model.MetadataAndPayload;
import net.consensys.mahuta.exception.MahutaException;

@Slf4j
@Ignore
public class IntegrationTest {
    private static final String endpoint = "http://localhost:8040";
    private static final String indexName = "test";
    
    private MahutaClient mahuta;
    
    @Before
    public void setup() {
        mahuta = new MahutaClient(endpoint, indexName);
    }
    @Test 
    public void storeAndSearchByHash() throws MahutaException {
        String fileName = "pdf-sample.pdf";
        String expectedHash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String contentType = "application/pdf";
        
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        IdAndHash idAndHash = mahuta.index(is, indexName, null, contentType);
        assertEquals(expectedHash, idAndHash.getHash());
        
        MetadataAndPayload result = mahuta.getByHash(indexName, idAndHash.getHash());
        
        assertEquals(expectedHash, result.getMetadata().getHash());
        assertEquals(contentType, result.getMetadata().getContentType());
        
    }
    
}
