package net.consensys.tools.ipfs.ipfsstore.client.java.wrapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreClientException;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;


/**
 * Integration layer to the IPFS-Store module
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
public interface IPFSStoreWrapper {

    /**
     * store
     * @param file
     * @return hash
     * @throws IPFSStoreClientException
     */
    String store(byte[] file) throws IPFSStoreClientException;
    
    /**
     * index
     * @param request
     * @return response
     * @throws IPFSStoreClientException
     */
    IndexerResponse index(IndexerRequest request) throws IPFSStoreClientException;
    
    /**
     * fetch
     * @param indexName
     * @param hash
     * @return file
     * @throws IPFSStoreClientException
     */
    byte[] fetch(String indexName, String hash) throws IPFSStoreClientException;
    
    /**
     * search
     * @param indexName
     * @param query
     * @param pageable
     * @return page of result
     * @throws IPFSStoreClientException
     */
    Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreClientException;
}
