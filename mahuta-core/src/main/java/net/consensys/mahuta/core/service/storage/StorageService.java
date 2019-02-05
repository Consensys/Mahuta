package net.consensys.mahuta.core.service.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import net.consensys.mahuta.core.service.pinning.PinningService;

public interface StorageService {

    Set<PinningService> getReplicaSet();
    
    String write(InputStream content);
    
    String write(byte[] content);

    OutputStream read(String id);
    
    OutputStream read(String id, OutputStream output);
}
