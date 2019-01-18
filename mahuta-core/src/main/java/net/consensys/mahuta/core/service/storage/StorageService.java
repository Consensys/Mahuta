package net.consensys.mahuta.core.service.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageService {

    String write(InputStream content);
    
    String write(byte[] content);
    
    void pin(String id);
    
    void unpin(String id);
    
    List<String> getPinned();

    OutputStream read(String id);
    
    OutputStream read(String id, OutputStream output);
}
