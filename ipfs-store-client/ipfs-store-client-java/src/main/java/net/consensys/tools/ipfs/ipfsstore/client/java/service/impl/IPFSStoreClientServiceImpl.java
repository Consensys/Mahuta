package net.consensys.tools.ipfs.ipfsstore.client.java.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
    
    private IPFSStoreWrapper wrapper;
    
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
    public OutputStream get(String indexName, String hash) throws IPFSStoreClientException {
        byte[] bytes = this.wrapper.fetch(indexName, hash);
        
        OutputStream os = new ByteArrayOutputStream();
        try {
            os.write(bytes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return os;
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

}
