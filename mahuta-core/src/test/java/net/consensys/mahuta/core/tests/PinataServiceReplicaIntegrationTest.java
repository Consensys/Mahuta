package net.consensys.mahuta.core.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import net.consensys.mahuta.core.service.pinning.ipfs.PinataPinningService;

@Ignore("Only run locally")
public class PinataServiceReplicaIntegrationTest {

    private static final String API_KEY = "CHANGE ME";
    private static final String SECRET_API_KEY = "CHANGE ME";
    private static final String CID = "Qmaisz6NMhDB51cCvNWa1GMS7LU1pAxdF4Ld6Ft9kZEP2a";
    
    public PinataServiceReplicaIntegrationTest() {
        
    }
    
    @Test
    public void connect() {
        PinataPinningService service = buildPinataPinningService(API_KEY, SECRET_API_KEY);
        assertNotNull(service);
    }
    
    @Test
    public void pin() {
        PinataPinningService service = buildPinataPinningService(API_KEY, SECRET_API_KEY);
        service.pin(CID);
        
        List<String> tracked = service.getTracked();
        assertTrue(tracked.stream().anyMatch(c -> c.equals(CID)));
    }
    
    @Test
    public void unpin() {
        PinataPinningService service = buildPinataPinningService(API_KEY, SECRET_API_KEY);
        service.pin(CID);
        service.unpin(CID);
        
        List<String> tracked = service.getTracked();
        assertFalse(tracked.stream().anyMatch(c -> c.equals(CID)));
    }
    
    
    
    
    private static PinataPinningService buildPinataPinningService(String apiKey, String secretKey) {
        return PinataPinningService.connect(apiKey, secretKey);
    }
}
