package net.consensys.mahuta.client.java.wrapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import net.consensys.mahuta.dto.IndexerRequest;
import net.consensys.mahuta.dto.IndexerResponse;
import net.consensys.mahuta.dto.Metadata;
import net.consensys.mahuta.dto.query.Query;
import net.consensys.mahuta.exception.MahutaException;


/**
 * Integration layer to the Mahuta module
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface MahutaWrapper {

    /**
     * store
     *
     * @param file
     * @return hash
     * @throws IPFSStoreException
     */
    String store(byte[] file) throws MahutaException;

    /**
     * index
     *
     * @param request
     * @return response
     * @throws IPFSStoreException
     */
    IndexerResponse index(IndexerRequest request) throws MahutaException;

    /**
     * store & index
     *
     * @param file
     * @param request
     * @return response
     * @throws IPFSStoreException
     */
    IndexerResponse storeAndIndex(byte[] file, IndexerRequest request) throws MahutaException;

    /**
     * fetch
     *
     * @param indexName
     * @param hash
     * @return file
     * @throws IPFSStoreException
     */
    byte[] fetch(String indexName, String hash) throws MahutaException;

    /**
     * search
     *
     * @param indexName
     * @param query
     * @param pageable
     * @return page of result
     * @throws IPFSStoreException
     */
    Page<Metadata> search(String indexName, Query query, Pageable pageable) throws MahutaException;

    /**
     * create index
     *
     * @param indexName
     * @throws IPFSStoreException
     */
    void createIndex(String indexName) throws MahutaException;

    /**
     * get client
     *
     * @return Rest Client
     */
    RestTemplate getClient();
}
