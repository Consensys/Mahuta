package net.consensys.tools.ipfs.ipfsstore.dao.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;
import net.consensys.tools.ipfs.ipfsstore.utils.Strings;

/**
 * IPFS implementation of StorageDao
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
@Service
public class IPFSStorageDao implements StorageDao {

    private static final Logger LOGGER = Logger.getLogger(IPFSStorageDao.class);
    
    private IPFS ipfs;

    @Autowired
    public IPFSStorageDao(IPFS ipfs) {
        this.ipfs = ipfs;
    }
    
    @Override
    public String createContent(byte[] content) throws DaoException {
        
        LOGGER.debug("Store file in IPFS ...");
        
        // Validation
        if(content == null) throw new IllegalArgumentException("content cannot be null");
        
        try {
            NamedStreamable.ByteArrayWrapper requestFile = new NamedStreamable.ByteArrayWrapper(content);
            MerkleNode response = this.ipfs.add(requestFile).get(0);
                        
            String hash = response.hash.toString();

            LOGGER.debug("Store created in IPFS [hash="+hash+"]");
            
            return hash;
            
        } catch (IOException ex) {
            LOGGER.error("Exception while storing file in IPFS", ex);
            throw new DaoException("Exception while storing file in IPFS: " + ex.getMessage());
        }
    }

    @Override
    public byte[] getContent(String hash) throws DaoException {
        
        LOGGER.debug("Get file in IPFS [hash="+hash+"] ...");
        
        // Validation
        if(Strings.isEmpty(hash)) throw new IllegalArgumentException("hash cannot be null or empty");

        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            byte[] content = this.ipfs.cat(filePointer);
            
            LOGGER.debug("Get file in IPFS [hash="+hash+"]");
            
            return content;
            
        } catch (IOException ex) {
            LOGGER.error("Exception while getting file in IPFS [hash="+hash+"]", ex);
            throw new DaoException("Exception while getting file in IPFS [hash="+hash+"]: " + ex.getMessage());
        }
    }

    @Override
    public void pin(String hash) throws DaoException {
        
        LOGGER.debug("pin file in IPFS [hash="+hash+"] ...");
        
        // Validation
        if(Strings.isEmpty(hash)) throw new IllegalArgumentException("hash cannot be null or empty");

        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            this.ipfs.pin.add(filePointer);
            
            LOGGER.debug("File pined in IPFS [hash="+hash+"]");
            
        } catch (IOException ex) {
            LOGGER.error("Exception while pining file in IPFS [hash="+hash+"]", ex);
            throw new DaoException("Exception while pining file in IPFS [hash="+hash+"]: " + ex.getMessage());
        }
    }

    @Override
    public void unpin(String hash) throws DaoException {
        
        LOGGER.debug("unpin file in IPFS [hash="+hash+"] ...");
        
        // Validation
        if(Strings.isEmpty(hash)) throw new IllegalArgumentException("hash cannot be null or empty");
        
        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            this.ipfs.pin.rm(filePointer);
            
            LOGGER.debug("File unpined in IPFS [hash="+hash+"]");
            
        } catch (IOException ex) {
            LOGGER.error("Exception while pining file in IPFS [hash="+hash+"]", ex);
            throw new DaoException("Exception while pining file in IPFS [hash="+hash+"]: " + ex.getMessage());
        }
        
    }

}
