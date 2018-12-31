package net.consensys.mahuta.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import net.consensys.mahuta.configuration.health.HealthCheck;
import net.consensys.mahuta.dto.IndexField;
import net.consensys.mahuta.dto.Metadata;
import net.consensys.mahuta.dto.query.Query;
import net.consensys.mahuta.exception.NotFoundException;

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
     * @param index Name of the index
     * @param documentId Document Identifier in the index (Optional, if null, auto-generated)
     * @param hash Content Unique Identifier
     * @param contentType Content Type (MIMETYPE)
     * @param indexFields index that file List of key/value attributes to index this content
     * @return Document Identifier
     */
    String index(String index, String documentId, String hash, String contentType,
            List<IndexField> indexFields);


    /**
     * Remove a document from the index
     * 
     * @param index Name of the index
     * @param documentId Document Identifier in the index
     */
    void deindex(String index, String documentId);
    
    /**
     * Search content by its unique identifier
     *
     * @param index Name of the index
     * @param id Document Identifier
     * @return File Metadata
     * @throws DaoException
     */
    Metadata searchById(Optional<String> index, String id) throws NotFoundException;

    /**
     * Search content in the index (index) based on a query
     *
     * @param pageable Pagination and Sorting
     * @param index Name of the index
     * @param query Search query
     * @return A list of File Metadata
     */
    List<Metadata> search(Pageable pageable, Optional<String> index, Query query);

    /**
     * Count content in the index (index) based on a query
     *
     * @param index Name of the index
     * @param query Search query
     * @return Total count of the search
     */
    long count(Optional<String> index, Query query);

    /**
     * Create an index
     *
     * @param index Name of the index
     */
    void createIndex(String index);

}
