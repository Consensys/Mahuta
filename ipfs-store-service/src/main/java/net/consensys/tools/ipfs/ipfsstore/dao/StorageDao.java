package net.consensys.tools.ipfs.ipfsstore.dao;

import net.consensys.tools.ipfs.ipfsstore.configuration.health.HealthCheck;
import net.consensys.tools.ipfs.ipfsstore.exception.TimeoutException;

/**
 * StorageDao represents an a set of methods to store a file in a filesystem
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface StorageDao extends HealthCheck {

    /**
     * Store content into the filesystem
     *
     * @param content Content of the file
     * @return Unique identifier of the file in the filesystem
     * @throws DaoException
     */
    String createContent(byte[] content);

    /**
     * Retrieve a file from the filesystem
     *
     * @param hash Unique identifier of the file
     * @return Content of the file
     * @throws DaoException
     */
    byte[] getContent(String hash) throws TimeoutException;


}
