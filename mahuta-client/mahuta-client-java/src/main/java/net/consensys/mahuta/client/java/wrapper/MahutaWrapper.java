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
     * @throws MahutaException
     */
    String store(byte[] file) throws MahutaException;

    /**
     * index
     *
     * @param request
     * @return response
     * @throws MahutaException
     */
    IndexerResponse index(IndexerRequest request) throws MahutaException;

    /**
     * store & index
     *
     * @param file
     * @param request
     * @return response
     * @throws MahutaException
     */
    IndexerResponse storeAndIndex(byte[] file, IndexerRequest request) throws MahutaException;

    /**
     * fetch
     *
     * @param indexName
     * @param hash
     * @return file
     * @throws MahutaException
     */
    byte[] fetch(String indexName, String hash) throws MahutaException;

    /**
     * search
     *
     * @param indexName
     * @param query
     * @param pageable
     * @return page of result
     * @throws MahutaException
     */
    Page<Metadata> search(String indexName, Query query, Pageable pageable) throws MahutaException;

    /**
     * create index
     *
     * @param indexName
     * @throws MahutaException
     */
    void createIndex(String indexName) throws MahutaException;

    /**
     * get client
     *
     * @return Rest Client
     */
    RestTemplate getClient();
}
