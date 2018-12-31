package net.consensys.mahuta.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.consensys.mahuta.dto.IndexerRequest;
import net.consensys.mahuta.dto.IndexerResponse;
import net.consensys.mahuta.dto.Metadata;
import net.consensys.mahuta.dto.query.Query;
import net.consensys.mahuta.exception.NotFoundException;
import net.consensys.mahuta.exception.TimeoutException;
import net.consensys.mahuta.exception.ValidationException;

/**
 * Storage Service gathers all the logic for the IPFS-Storage
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface StoreService {

    /**
     * Store a file in the target filesystem
     *
     * @param file File content (binary)
     * @return File Unique Identifier
     */
    String storeFile(byte[] file);
    /**
     * Index a file
     *
     * @param request Request containing metadata to index (ID, hash, type, index fields)
     * @return Response containing the tuple (index, index ID, file ID)
     * @throws ValidationException
     */
    IndexerResponse indexFile(IndexerRequest request) throws ValidationException;

    /**
     * Store a file and index it
     *
     * @param file File content (binary)
     * @param request Request containing metadata to index (ID, hash, type, index fields)
     * @return Request containing metadata to index (ID, hash, type, index fields)
     * @throws ValidationException
     */
    IndexerResponse storeAndIndexFile(byte[] file, IndexerRequest request)
            throws ValidationException;
    
    /**
     * Get Content by File Unique Identifier
     *
     * @param hash File Unique Identifier
     * @return File content (binary)
     * @throws TimeoutException
     */
    byte[] getFileByHash(String hash) throws TimeoutException;

    /**
     * Get Content Metadata by Index Unique Identifier
     *
     * @param index Index name
     * @param id Index Unique Identifier
     * @return Content Metadata (ID, hash, type, index fields)
     * @throws NotFoundException
     */
    Metadata getFileMetadataById(Optional<String> index, String id) throws NotFoundException;

    /**
     * Get Content Metadata by File Unique Identifier
     *
     * @param index Index name
     * @param hash File Unique Identifier
     * @return Content Metadata (ID, hash, type, index fields)
     * @throws NotFoundException
     */
    Metadata getFileMetadataByHash(Optional<String> index, String hash) throws NotFoundException;

    /**
     * Search in the index a list of content against a multi-criteria search query
     *
     * @param index Index name
     * @param query Query
     * @param pagination Pagination & Sorting
     * @return Page of Metadata result (ID, hash, type, index fields)
     */
    Page<Metadata> searchFiles(Optional<String> index, Query query, Pageable pagination);

    /**
     * Remove a file from the filesystem
     * @param index Index name
     * @param hash File Unique Identifier
     * @throws NotFoundException
     */
    void removeFileByHash(String index, String hash) throws NotFoundException ;
    
    /**
     * Remove a file from the filesystem
     * @param index Index name
     * @param id Index Document Unique Identifier
     * @throws NotFoundException
     */
    void removeFileById(String index, String id) throws NotFoundException ;

    /**
     * Create an index
     *
     * @param index Index name
     */
    void createIndex(String index);
}
