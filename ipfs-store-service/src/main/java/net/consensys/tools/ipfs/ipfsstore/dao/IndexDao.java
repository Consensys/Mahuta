package net.consensys.tools.ipfs.ipfsstore.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import net.consensys.tools.ipfs.ipfsstore.configuration.health.HealthCheck;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;

/**
 * IndexerDao represents an a set of methods to index content in a SarchEngine (index)
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface IndexDao extends HealthCheck {

    /**
     * Special keys
     */
    String HASH_INDEX_KEY = "__hash";
    String CONTENT_TYPE_INDEX_KEY = "__content_type";

    /**
     * Index a content
     *
     * @param index
     *            Name of the index
     * @param documentId
     *            Document Identifier in the index (Optional, if null, auto-generated)
     * @param hash
     *            Content Unique Identifier
     * @param contentType
     *            Content Type (MIMETYPE)
     * @param indexFields
     *            index that file List of key/value attributes to index this content
     * @return Document Identifier
     * @throws DaoException
     */
    String index(String index, String documentId, String hash, String contentType,
            List<IndexField> indexFields);

    /**
     * Search content by its unique identifier
     *
     * @param index
     *            Name of the index
     * @param id
     *            Document Identifier
     * @return File Metadata
     * @throws DaoException
     */
    Metadata searchById(Optional<String> index, String id) throws NotFoundException;

    /**
     * Search content in the index (index) based on a query
     *
     * @param pageable
     *            Pagination and Sorting
     * @param index
     *            Name of the index
     * @param query
     *            Search query
     * @return A list of File Metadata
     * @throws DaoException
     */
    List<Metadata> search(Pageable pageable, Optional<String> index, Query query);

    /**
     * Count content in the index (index) based on a query
     *
     * @param index
     *            Name of the index
     * @param query
     *            Search query
     * @return Total count of the search
     * @throws DaoException
     */
    long count(Optional<String> index, Query query);

    /**
     * Create an index
     *
     * @param index
     *            Name of the index
     * @throws DaoException
     */
    void createIndex(String index);

}
