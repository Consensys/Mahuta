package net.consensys.tools.ipfs.ipfsstore.client.java.wrapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;


/**
 * Integration layer to the IPFS-Store module
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface IPFSStoreWrapper {

    /**
     * store
     *
     * @param file
     * @return hash
     * @throws IPFSStoreException
     */
    String store(byte[] file) throws IPFSStoreException;

    /**
     * index
     *
     * @param request
     * @return response
     * @throws IPFSStoreException
     */
    IndexerResponse index(IndexerRequest request) throws IPFSStoreException;

    /**
     * store & index
     *
     * @param file
     * @param request
     * @return response
     * @throws IPFSStoreException
     */
    IndexerResponse storeAndIndex(byte[] file, IndexerRequest request) throws IPFSStoreException;

    /**
     * fetch
     *
     * @param indexName
     * @param hash
     * @return file
     * @throws IPFSStoreException
     */
    byte[] fetch(String indexName, String hash) throws IPFSStoreException;

    /**
     * search
     *
     * @param indexName
     * @param query
     * @param pageable
     * @return page of result
     * @throws IPFSStoreException
     */
    Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreException;

    /**
     * create index
     *
     * @param indexName
     * @throws IPFSStoreException
     */
    void createIndex(String indexName) throws IPFSStoreException;

    /**
     * get client
     *
     * @return Rest Client
     */
    RestTemplate getClient();
}
