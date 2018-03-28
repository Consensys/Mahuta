package net.consensys.tools.ipfs.ipfsstore.client.springdata;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * IPFSStoreRepository represents the union of a PagingAndSortingRepository and a IPFSStoreCustomRepository
 *
 * @param <E>  Entity type
 * @param <ID> Entity ID type
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface IPFSStoreRepository<E, ID extends Serializable> extends PagingAndSortingRepository<E, ID>, IPFSStoreCustomRepository<E, ID> {

    /**
     * Default Page No
     */
    int DEFAULT_PAGE_NO = 0;

    /**
     * Default Page Size
     */
    int DEFAULT_PAGE_SIZE = 20;

    /**
     * Save a document with external indexes
     *
     * @param entity Entity to save
     * @return Entity saved
     */
    <S extends E> S save(S entity, Map<String, Object> externalIndexFields);
}

