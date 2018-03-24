package net.consensys.tools.ipfs.ipfsstore.client.springdata;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * IPFSStoreCustomRepository allows to extend a classic CRUDRepository by providing custom methods
 *
 * @param <E>  Entity type
 * @param <ID> Entity ID type
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface IPFSStoreCustomRepository<E, ID extends Serializable> {

    /**
     * Find elements in the repository by full text search
     *
     * @param fullTextCriteria Criteria
     * @param pagination       Pagination
     * @return A Page of result
     */
    Page<E> findByfullTextSearch(String fullTextCriteria, Pageable pagination);

    /**
     * Find a document in the repository by hash
     *
     * @param hash
     * @return
     */
    E findByHash(String hash);

    /**
     * Save a document without attributing an ID and a Hash to the object (no reflection)
     *
     * @param entity Entity to save
     * @return Unique hash of the file
     */
    String saveWithoutAutoSetup(E entity);
}


