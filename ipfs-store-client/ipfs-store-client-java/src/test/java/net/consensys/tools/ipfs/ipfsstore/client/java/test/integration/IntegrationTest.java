/**
 * 
 */
package net.consensys.tools.ipfs.ipfsstore.client.java.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.java.model.IdAndHash;
import net.consensys.tools.ipfs.ipfsstore.client.java.model.MetadataAndPayload;
import net.consensys.tools.ipfs.ipfsstore.exception.IPFSStoreException;

@Slf4j
@Ignore
public class IntegrationTest {
    private static final String endpoint = "http://localhost:8040";
    private static final String indexName = "test";
    
    private IPFSStore ipfsStore;
    
    @Before
    public void setup() {
        ipfsStore = new IPFSStore(endpoint, indexName);
    }
    @Test 
    public void storeAndSearchByHash() throws IPFSStoreException {
        String fileName = "pdf-sample.pdf";
        String expectedHash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String contentType = "application/pdf";
        
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        IdAndHash idAndHash = ipfsStore.index(is, indexName, null, contentType);
        assertEquals(expectedHash, idAndHash.getHash());
        
        MetadataAndPayload result = ipfsStore.getByHash(indexName, idAndHash.getHash());
        
        assertEquals(expectedHash, result.getMetadata().getHash());
        assertEquals(contentType, result.getMetadata().getContentType());
        
    }
    
}
