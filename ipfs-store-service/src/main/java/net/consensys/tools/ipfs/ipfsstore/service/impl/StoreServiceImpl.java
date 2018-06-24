package net.consensys.tools.ipfs.ipfsstore.service.impl;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.configuration.PinningConfiguration;
import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.exception.TimeoutException;
import net.consensys.tools.ipfs.ipfsstore.exception.ValidationException;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;

/**
 * Implementation of StoreService
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Service
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final Validator validator;

    private final IndexDao indexDao;
    private final StorageDao storageDao;
    private final PinningConfiguration pinningConfiguration;

    @Autowired
    public StoreServiceImpl(IndexDao indexDao, StorageDao storageDao,
            PinningConfiguration pinningConfiguration) {
        this.indexDao = indexDao;
        this.storageDao = storageDao;
        this.pinningConfiguration = pinningConfiguration;

        // Validator
        Configuration<?> config = Validation.byDefaultProvider().configure();
        ValidatorFactory factory = config.buildValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Override
    public String storeFile(byte[] file) {

        // Store file
        String hash = this.storageDao.createContent(file);

        // pin file
        pinningConfiguration.getPinningStrategies().forEach((pinStrategy) -> {
            CompletableFuture.supplyAsync(() -> {
                log.debug("Executing async pin_service [name={}] for hash {}",
                        pinStrategy.getName(), hash);
                pinStrategy.pin(hash);
                return true;
            });
        });

        return hash;
    }

    @Override
    public IndexerResponse indexFile(IndexerRequest request) throws ValidationException {

        validate(request);

        log.trace(request.toString());

        indexDao.createIndex(request.getIndex()); // Create the index if it doesn't exist

        String documentId = indexDao.index(request.getIndex(), request.getDocumentId(),
                request.getHash(), request.getContentType(), request.getIndexFields());

        return new IndexerResponse(request.getIndex(), documentId, request.getHash());
    }

    @Override
    public IndexerResponse storeAndIndexFile(byte[] file, IndexerRequest request)
            throws ValidationException {

        // Store the file
        String hash = this.storeFile(file);
        request.setHash(hash);

        // Index it
        return this.indexFile(request);
    }

    @Override
    public byte[] getFileByHash(String hash) throws TimeoutException {

        return this.storageDao.getContent(hash);
    }

    @Override
    public Metadata getFileMetadataById(Optional<String> index, String id)
            throws NotFoundException {

        return this.indexDao.searchById(index, id);
    }

    @Override
    public Metadata getFileMetadataByHash(Optional<String> index, String hash)
            throws NotFoundException {

        Query query = new Query().equals(IndexDao.HASH_INDEX_KEY, hash.toLowerCase()); // TODO ES
                                                                                       // case
                                                                                       // sensitive
                                                                                       // analyser
        Page<Metadata> search = this.searchFiles(index, query, new PageRequest(0, 1));

        if (search.getTotalElements() == 0) {
            throw new NotFoundException(
                    "File [hash=" + hash + "] not found in the index [" + index + "]");
        }

        return search.getContent().get(0);
    }

    @Override
    public void createIndex(String index) {

        this.indexDao.createIndex(index);
    }

    @Override
    public Page<Metadata> searchFiles(Optional<String> index, Query query, Pageable pageable) {

        return new PageImpl<>(indexDao.search(pageable, index, query), pageable,
                indexDao.count(index, query));
    }

    /**
     * Validate an object
     *
     * @param object
     * @throws ServiceException
     */
    private <O> void validate(O object) throws ValidationException {
        Set<ConstraintViolation<O>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.toString());
        }
    }
}
