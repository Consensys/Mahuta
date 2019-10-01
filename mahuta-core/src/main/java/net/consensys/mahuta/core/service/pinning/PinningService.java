package net.consensys.mahuta.core.service.pinning;

import java.util.List;

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
