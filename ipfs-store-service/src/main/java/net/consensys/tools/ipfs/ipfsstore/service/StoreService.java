package net.consensys.tools.ipfs.ipfsstore.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.exception.TimeoutException;
import net.consensys.tools.ipfs.ipfsstore.exception.ValidationException;

/**
 * Storage Service gathers all the logic for the IPFS-Storage
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface StoreService {

    /**
     * Store a file in the target filesystem
     *
     * @param file
     *            File content (binary)
     * @return File Unique Identifier
     * @throws ServiceException
     */
    String storeFile(byte[] file);

    /**
     * Index a file
     *
     * @param request
     *            Request containing metadata to index (ID, hash, type, index fields)
     * @return Response containing the tuple (index, index ID, file ID)
     * @throws ServiceException
     */
    IndexerResponse indexFile(IndexerRequest request) throws ValidationException;

    /**
     * Store a file and index it
     *
     * @param file
     *            File content (binary)
     * @param request
     *            Request containing metadata to index (ID, hash, type, index fields)
     * @return Request containing metadata to index (ID, hash, type, index fields)
     * @throws ServiceException
     */
    IndexerResponse storeAndIndexFile(byte[] file, IndexerRequest request)
            throws ValidationException;

    /**
     * Get Content by File Unique Identifier
     *
     * @param hash
     *            File Unique Identifier
     * @return File content (binary)
     * @throws ServiceException
     */
    byte[] getFileByHash(String hash) throws TimeoutException;

    /**
     * Get Content Metadata by Index Unique Identifier
     *
     * @param index
     *            Index name
     * @param id
     *            Index Unique Identifier
     * @return Content Metadata (ID, hash, type, index fields)
     * @throws ServiceException
     */
    Metadata getFileMetadataById(Optional<String> index, String id) throws NotFoundException;

    /**
     * Get Content Metadata by File Unique Identifier
     *
     * @param index
     *            Index name
     * @param hash
     *            File Unique Identifier
     * @return Content Metadata (ID, hash, type, index fields)
     * @throws ServiceException
     */
    Metadata getFileMetadataByHash(Optional<String> index, String hash) throws NotFoundException;

    /**
     * Search in the index a list of content against a multi-criteria search query
     *
     * @param index
     *            Index name
     * @param query
     *            Query
     * @param pagination
     *            Pagination & Sorting
     * @return Page of Metadata result (ID, hash, type, index fields)
     * @throws ServiceException
     */
    Page<Metadata> searchFiles(Optional<String> index, Query query, Pageable pagination);

    /**
     * Create an index
     *
     * @param index
     *            Index name
     * @throws ServiceException
     */
    void createIndex(String index);
}
