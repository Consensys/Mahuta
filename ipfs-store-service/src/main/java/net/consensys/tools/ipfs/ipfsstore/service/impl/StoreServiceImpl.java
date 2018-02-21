package net.consensys.tools.ipfs.ipfsstore.service.impl;

import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.exception.ServiceException;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;

/**
 * Implementation of StoreService
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
@Service
public class StoreServiceImpl implements StoreService {
    
    private static final Logger LOGGER = Logger.getLogger(StoreServiceImpl.class);

    private Validator validator;
    
    private IndexDao indexDao;
    private StorageDao storageDao;
    
    @Autowired
    public StoreServiceImpl(IndexDao indexDao, StorageDao storageDao) {
        this.indexDao = indexDao;
        this.storageDao = storageDao;
        
        // Validator
        Configuration<?> config = Validation.byDefaultProvider().configure();
        ValidatorFactory factory = config.buildValidatorFactory();
        this.validator = factory.getValidator();
    }
    


    @Override
    public String storeFile(byte[] file) throws ServiceException {
        
        try {
            return this.storageDao.createContent(file);  
            
        } catch (DaoException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        }
    }    

    @Override
    public IndexerResponse indexFile(IndexerRequest request) throws ServiceException {
        
        validate(request);
        
        try {
            indexDao.createIndex(request.getIndexName()); // Create the index if it doesn't exist
            
            String documentId = indexDao.index(
                    request.getIndexName(), 
                    request.getDocumentId(), 
                    request.getHash(), 
                    request.getContentType(), 
                    request.getIndexFields());
            
            return new IndexerResponse(request.getIndexName(), documentId, request.getHash());
            
        } catch (DaoException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        }
    }
    
    
    @Override
    public IndexerResponse storeAndIndexFile(byte[] file, IndexerRequest request) throws ServiceException {

        try {
            // Store the file
            String hash = this.storeFile(file);
            request.setHash(hash);
            
            // Index it
            return this.indexFile(request);
            
        } catch (ServiceException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        } 
    }

    @Override
    public byte[] getFileByHash(String hash) throws ServiceException {
        
        try {
            return this.storageDao.getContent(hash);  
            
        } catch (DaoException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        }
    }

    @Override
    public Metadata getFileMetadataById(String index, String id) throws ServiceException {
        
        try {
            return this.indexDao.searchById(index, id); 
            
        } catch (DaoException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        }
    }

    @Override
    public Metadata getFileMetadataByHash(String index, String hash) throws ServiceException {
        
        try {
            Query query = new Query();
            query.equals(IndexDao.HASH_INDEX_KEY, hash.toLowerCase()); // TODO ES case sensitive analyser
            Page<Metadata> search = this.searchFiles(index, query, new PageRequest(1, 1));
            
            if(search.getTotalElements() == 0) {
                throw new NotFoundException("File [hash="+hash+"] not found in the index ["+index+"]");
            }
            
            return search.getContent().get(0);
            
        } catch (DaoException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        }
    }

    @Override
    public Page<Metadata> searchFiles(String index, Query query, Pageable pageable) throws ServiceException {
        
        try {            
            Page<Metadata> page = new PageImpl<>(
                    indexDao.search(pageable, index, query), 
                    pageable, 
                    indexDao.count(index, query));
           
            return page;
            
        } catch (DaoException ex) {
            LOGGER.error("Exception occur:", ex);
            throw new ServiceException(ex.getMessage());
        } 
    }
    
    /**
     * Validate an object
     * @param object
     * @throws ServiceException
     */
    private <O> void validate(O object) throws ServiceException {
        Set<ConstraintViolation<O>> violations = validator.validate(object);
        if (violations.size() > 0) {
            throw new ServiceException(violations.toString());
        } 
    }
}
