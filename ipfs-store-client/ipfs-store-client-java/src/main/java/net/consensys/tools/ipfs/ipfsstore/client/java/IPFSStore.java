package net.consensys.tools.ipfs.ipfsstore.client.java;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.IPFSStoreWrapper;
import net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.impl.RestIPFSStoreWrapperImpl;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;

/**
 * IPFS Store Java Client
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
public class IPFSStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStore.class);
    
    private static final String ID_ATTRIBUTE = "_id";
    
    private final IPFSStoreWrapper wrapper;
    
    public IPFSStore(String endpoint) {
        this.wrapper = new RestIPFSStoreWrapperImpl(endpoint);
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IPFSStoreException
     */
    public String store(String filePath) throws IPFSStoreException {
        
        try {
            return this.store(new FileInputStream(filePath));
            
        } catch(FileNotFoundException e) {
            throw new IPFSStoreException(e);
        }
    }

    public String store(InputStream is) throws IPFSStoreException {
        try {
            return this.wrapper.store(IOUtils.toByteArray(is));
            
        } catch(IOException e) {
            throw new IPFSStoreException(e);
        }
    }

    public String index(String indexName, String hash) throws IPFSStoreException {
        return this.index(indexName, hash, null);
    }

    public String index(String indexName, String hash, String id) throws IPFSStoreException {
        return this.index(indexName, hash, id, null);
    }

    public String index(String indexName, String hash, String id, String contentType) throws IPFSStoreException {
        return this.index(indexName, hash, id, contentType, new ArrayList<>());
    }

    public String index(String indexName, String hash, String id, String contentType, Map<String, Object> indexFields)
            throws IPFSStoreException {
        
        return this.index(indexName, hash, id, contentType, convert(indexFields));
    }

    public String index(String indexName, String hash, String id, String contentType, List<IndexField> indexFields)
            throws IPFSStoreException {

        return this.wrapper.index(createRequest(indexName, hash, id, contentType, indexFields)).
                getDocumentId();
    }

    public String index(InputStream file, String indexName) throws IPFSStoreException {
        return this.index(file, indexName, null);
    }

    public String index(InputStream file, String indexName, String id) throws IPFSStoreException {
        return this.index(file, indexName, id, null);
    }

    public String index(InputStream file, String indexName, String id, String contentType) throws IPFSStoreException {
        return this.index(file, indexName, id, contentType, new ArrayList<>());
    }

    public String index(InputStream file, String indexName, String id, String contentType,
            Map<String, Object> indexFields) throws IPFSStoreException {
        return this.index(file, indexName, id, contentType, convert(indexFields));
    }

    public String index(InputStream file, String indexName, String id, String contentType,
            List<IndexField> indexFields) throws IPFSStoreException {
         
        try {
            return this.wrapper.storeAndIndex(IOUtils.toByteArray(file), createRequest(indexName, null, id, contentType, indexFields)).
                    getDocumentId();
            
        } catch(IOException e) {
            throw new IPFSStoreException(e);
        }
    }

    public byte[]  get(String indexName, String hash) throws IPFSStoreException {
        return this.wrapper.fetch(indexName, hash);
    }

    public byte[] getById(String indexName, String id) throws IPFSStoreException {
        return this.get(indexName, this.getMetadataById(indexName, id).getHash());
    }


    public Metadata getMetadataById(String indexName, String id) throws IPFSStoreException {
        Query query = Query.newQuery();
        query.equals(ID_ATTRIBUTE, id);
        
        Page<Metadata> searchResult = this.wrapper.search(indexName, query, new PageRequest(1, 1));
        if(searchResult.getTotalElements() == 0) {
            LOGGER.warn("Content [indexName="+indexName+", id="+id+"] not found");
            return null;
        }
        
        return searchResult.getContent().get(0);
    }
    
    public Page<Metadata> search(String indexName) throws IPFSStoreException {
        return this.search(indexName, null);
    }

    public Page<Metadata> search(String indexName, Query query) throws IPFSStoreException {
        return this.search(indexName, query, null);
    }

    public Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreException {
        return this.wrapper.search(indexName, query, pageable);
    }

    public Page<Metadata> search(String indexName, Query query, int pageNo, int pageSize)
            throws IPFSStoreException {
        return this.search(indexName, query, pageNo, pageSize, null, null);
    }

    public Page<Metadata> search(String indexName, Query query, int pageNo, int pageSize, String sortAttribute,
            Direction sortDirection) throws IPFSStoreException {
        
        PageRequest pagination = null;
        if(sortAttribute == null || sortAttribute.isEmpty()) {
            pagination = new PageRequest(pageNo, pageSize);
        } else {
            pagination = new PageRequest(pageNo, pageSize, new Sort(sortDirection, sortAttribute));
        }
        
        return this.search(indexName, query, pagination);
    }

    public Page<byte[]> searchAndFetch(String indexName) throws IPFSStoreException {
        return this.searchAndFetch(indexName, null);
    }

    public Page<byte[]> searchAndFetch(String indexName, Query query) throws IPFSStoreException {
        return this.searchAndFetch(indexName, query, null);
    }
    
    public Page<byte[]> searchAndFetch(String indexName, Query query, Pageable pageable)
            throws IPFSStoreException {
        Page<Metadata> search = this.wrapper.search(indexName, query, pageable);
        
        List<byte[]> contentList = search.getContent().stream().map(m->{
            try {
                return this.get(indexName, m.getHash());
            } catch (IPFSStoreException e) {
                LOGGER.error("Error while fetching " + m.getHash(), e);
                return null;
            }
        }).collect(Collectors.toList());
        
        return new PageImpl<>(contentList, pageable, search.getTotalElements());
    }

    public Page<byte[]> searchAndFetch(String indexName, Query query, int pageNo, int pageSize)
            throws IPFSStoreException {
        return this.searchAndFetch(indexName, query, pageNo, pageSize, null, null);
    }

    public Page<byte[]> searchAndFetch(String indexName, Query query, int pageNo, int pageSize, String sortAttribute,
            Direction sortDirection) throws IPFSStoreException {
        
        PageRequest pagination = null;
        if(sortAttribute == null || sortAttribute.isEmpty()) {
            pagination = new PageRequest(pageNo, pageSize);
        } else {
            pagination = new PageRequest(pageNo, pageSize, new Sort(sortDirection, sortAttribute));
        }
        
        return this.searchAndFetch(indexName, query, pagination);
    }
    
    private static List<IndexField> convert(Map<String, Object> indexFields) {
        return indexFields.entrySet().stream()
                .map(e -> new IndexField(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
    
    private static IndexerRequest createRequest(String indexName, String hash, String id, String contentType, List<IndexField> indexFields) {
        IndexerRequest request = new IndexerRequest();
        request.setIndexName(indexName);
        request.setHash(hash);
        request.setDocumentId(id);
        request.setContentType(contentType);
        request.setIndexFields(indexFields);
        
        return request;
    }
    
    public IPFSStoreWrapper getWrapper() {
        return this.wrapper;
    }

}
