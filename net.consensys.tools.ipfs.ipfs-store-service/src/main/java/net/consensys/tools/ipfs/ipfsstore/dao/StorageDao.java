package net.consensys.tools.ipfs.ipfsstore.dao;

import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;

/**
 * StorageDao represents an a set of methods to store a file in a filesystem
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
public interface StorageDao {
    
    /**
     * Store content into the filesystem
     * 
     * @param     content     Content of the file
     * @return                Unique identifier of the file in the filesystem
     *     
     * @throws DaoException
     */
    String createContent(byte[] content) throws DaoException;
    
    /**
     * Retrieve a file from the filesystem
     * 
     * @param     hash     Unique identifier of the file
     * @return             Content of the file
     * 
     * @throws DaoException
     */
    byte[] getContent(String hash) throws DaoException;
    
    /**
     * Pin a file to insure it is not garbage collected
     * 
     * @param hash     Unique identifier of the file
     * 
     * @throws DaoException
     */
    void pin(String hash) throws DaoException;
    
    /**
     * Unpin a file and tell the filesystem it can be garbage collected
     * 
     * @param hash     Unique identifier of the file
     * 
     * @throws DaoException
     */
    void unpin(String hash) throws DaoException;
}
