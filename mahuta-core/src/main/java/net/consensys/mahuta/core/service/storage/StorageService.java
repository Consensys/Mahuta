package net.consensys.mahuta.core.service.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import net.consensys.mahuta.core.service.pinning.PinningService;

/**
 * Interface representing a storage layer
 * 
 * @author gjeanmart<gregoire.jeanmart@gmail.com>
 *
 */
public interface StorageService {

    /**
     * Return the ReplicatSet configuration (list of services able to pin a file)
     * @return ReplicaSet   List of PinningService
     */
    Set<PinningService> getReplicaSet();
   
    /**
     * Write content on the storage layer
     * @param content InputStream
     * @param noPin Disable persistence, require to pin/persist asynchrounsly (can improve the writing performance)
     * @return Content ID (hash, CID)
     */
    String write(InputStream content, boolean noPin);

    /**
     * Write content on the storage layer
     * @param content Byte array
     * @param noPin Disable persistence, require to pin/persist asynchrounsly (can improve the writing performance)
     * @return Content ID (hash, CID)
     */
    String write(byte[] content, boolean noPin);

    /**
     * Read content from the storage layer and write it in a ByteArrayOutputStream
     * @param id Content ID (hash, CID
     * @return content
     */
    OutputStream read(String id);
    
    /**
     *  Read content from the storage layer and write it the OutputStream provided
     * @param id Content ID (hash, CID
     * @param output OutputStream to write content to
     * @return Outputstream passed as argument
     */
    OutputStream read(String id, OutputStream output);
}
