package net.consensys.tools.ipfs.ipfsstore.client.java.service.impl;

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

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreClientException;
import net.consensys.tools.ipfs.ipfsstore.client.java.service.IPFSStoreClientService;
import net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.IPFSStoreWrapper;
import net.consensys.tools.ipfs.ipfsstore.client.java.wrapper.impl.RestIPFSStoreWrapperImpl;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;

public class IPFSStoreClientServiceImpl implements IPFSStoreClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreClientServiceImpl.class);
    
    private final IPFSStoreWrapper wrapper;
    
    public IPFSStoreClientServiceImpl(String endpoint) {
        this.wrapper = new RestIPFSStoreWrapperImpl(endpoint);
    }
    
    @Override
    public String store(String filePath) throws IPFSStoreClientException {
        
        try {
            return this.store(new FileInputStream(filePath));
            
        } catch(FileNotFoundException e) {
            throw new IPFSStoreClientException(e);
        }
    }

    @Override
    public String store(InputStream is) throws IPFSStoreClientException {
        try {
            return this.wrapper.store(IOUtils.toByteArray(is));
            
        } catch(IOException e) {
            throw new IPFSStoreClientException(e);
        }
    }

    @Override
    public String index(String indexName, String hash) throws IPFSStoreClientException {
        return this.index(indexName, hash, null);
    }

    @Override
    public String index(String indexName, String hash, String id) throws IPFSStoreClientException {
        return this.index(indexName, hash, id, null);
    }

    @Override
    public String index(String indexName, String hash, String id, String contentType) throws IPFSStoreClientException {
        return this.index(indexName, hash, id, contentType, new ArrayList<>());
    }

    @Override
    public String index(String indexName, String hash, String id, String contentType, Map<String, Object> indexFields)
            throws IPFSStoreClientException {
        
        return this.index(indexName, hash, id, contentType, convert(indexFields));
    }

    @Override
    public String index(String indexName, String hash, String id, String contentType, List<IndexField> indexFields)
            throws IPFSStoreClientException {

        return this.wrapper.index(createRequest(indexName, hash, id, contentType, indexFields)).
                getDocumentId();
    }

    @Override
    public String index(InputStream file, String indexName) throws IPFSStoreClientException {
        return this.index(file, indexName, null);
    }

    @Override
    public String index(InputStream file, String indexName, String id) throws IPFSStoreClientException {
        return this.index(file, indexName, id, null);
    }

    @Override
    public String index(InputStream file, String indexName, String id, String contentType) throws IPFSStoreClientException {
        return this.index(file, indexName, id, contentType, new ArrayList<>());
    }

    @Override
    public String index(InputStream file, String indexName, String id, String contentType,
            Map<String, Object> indexFields) throws IPFSStoreClientException {
        return this.index(file, indexName, id, contentType, convert(indexFields));
    }

    @Override
    public String index(InputStream file, String indexName, String id, String contentType,
            List<IndexField> indexFields) throws IPFSStoreClientException {
         
        return this.wrapper.index(createRequest(indexName, this.store(file), id, contentType, indexFields)).
                getDocumentId();
    }

    @Override
    public byte[]  get(String indexName, String hash) throws IPFSStoreClientException {
        return this.wrapper.fetch(indexName, hash);
    }

    @Override
    public byte[] getById(String indexName, String id) throws IPFSStoreClientException {
        return this.get(indexName, this.getMetadataById(indexName, id).getHash());
    }


    @Override
    public Metadata getMetadataById(String indexName, String id) throws IPFSStoreClientException {
        Query query = Query.newQuery();
        query.equals("id", id);
        
        Page<Metadata> searchResult = this.wrapper.search(indexName, query, new PageRequest(1, 1));
        if(searchResult.getTotalElements() == 0) {
            LOGGER.warn("Content [indexName="+indexName+", id="+id+"] not found");
            return null;
        }
        
        return searchResult.getContent().get(0);
    }
    
    @Override
    public Page<Metadata> search(String indexName) throws IPFSStoreClientException {
        return this.search(indexName, null);
    }

    @Override
    public Page<Metadata> search(String indexName, Query query) throws IPFSStoreClientException {
        return this.search(indexName, query, null);
    }

    @Override
    public Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreClientException {
        return this.wrapper.search(indexName, query, pageable);
    }

    @Override
    public Page<Metadata> search(String indexName, Query query, int pageNo, int pageSize)
            throws IPFSStoreClientException {
        return this.search(indexName, query, pageNo, pageSize, null, null);
    }

    @Override
    public Page<Metadata> search(String indexName, Query query, int pageNo, int pageSize, String sortAttribute,
            Direction sortDirection) throws IPFSStoreClientException {
        
        PageRequest pagination = null;
        if(sortAttribute == null || sortAttribute.isEmpty()) {
            pagination = new PageRequest(pageNo, pageSize);
        } else {
            pagination = new PageRequest(pageNo, pageSize, new Sort(sortDirection, sortAttribute));
        }
        
        return this.search(indexName, query, pagination);
    }

    @Override
    public Page<byte[]> searchAndFetch(String indexName) throws IPFSStoreClientException {
        return this.searchAndFetch(indexName, null);
    }

    @Override
    public Page<byte[]> searchAndFetch(String indexName, Query query) throws IPFSStoreClientException {
        return this.searchAndFetch(indexName, query, null);
    }
    
    @Override
    public Page<byte[]> searchAndFetch(String indexName, Query query, Pageable pageable)
            throws IPFSStoreClientException {
        Page<Metadata> search = this.wrapper.search(indexName, query, pageable);
        
        List<byte[]> contentList = search.getContent().stream().map(m->{
            try {
                return this.get(indexName, m.getHash());
            } catch (IPFSStoreClientException e) {
                LOGGER.error("Error while fetching " + m.getHash(), e);
                return null;
            }
        }).collect(Collectors.toList());
        
        return new PageImpl<>(contentList, pageable, search.getTotalElements());
    }

    @Override
    public Page<byte[]> searchAndFetch(String indexName, Query query, int pageNo, int pageSize)
            throws IPFSStoreClientException {
        return this.searchAndFetch(indexName, query, pageNo, pageSize, null, null);
    }

    @Override
    public Page<byte[]> searchAndFetch(String indexName, Query query, int pageNo, int pageSize, String sortAttribute,
            Direction sortDirection) throws IPFSStoreClientException {
        
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
