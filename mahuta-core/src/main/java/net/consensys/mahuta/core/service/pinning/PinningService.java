package net.consensys.mahuta.core.service.pinning;

import java.util.List;
import java.util.Map;

/**
 * Interface representing a pinning service.
 * 
 * @author gjeanmart<gregoire.jeanmart@gmail.com>
 *
 */
public interface PinningService {

    /**
     * Return the name of the service
     * @return Name of the service
     */
    String getName();
    
    /**
     * Pin content 
     * @param id Content ID (hash, CID)
     */
    void pin(String id);
    
    /**
     * Pin content with metadata
     * default implement only pin CID
     * @param id Content ID (hash, CID)
     * @param name Content name
     * @param metadata Content metadata
     */
    default void pin(String id, String name, Map<String, Object> metadata) { 
        pin(id);
    } 
    
    /**
     * unpin content
     * @param id Content ID (hash, CID)
     */
    void unpin(String id);
    
    /**
     * Get list of all tracked files
     * @return List of Content ID
     */
    List<String> getTracked();
}
