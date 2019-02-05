package net.consensys.mahuta.core.service.pinning;

import java.util.List;

public interface PinningService {
    
    void pin(String id);
    
    void unpin(String id);
    
    List<String> getTracked();
}
