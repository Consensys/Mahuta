package net.consensys.tools.ipfs.ipfsstore.client.java.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreClientException;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexField;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;

public interface IPFSStoreClientService {

    String store(String filePath) throws IPFSStoreClientException;
    String store(InputStream file) throws IPFSStoreClientException;
    
    String index(String indexName, String hash) throws IPFSStoreClientException;
    String index(String indexName, String hash, String id) throws IPFSStoreClientException;
    String index(String indexName, String hash, String id, String contentType) throws IPFSStoreClientException;
    String index(String indexName, String hash, String id, String contentType, Map<String, Object> indexFields) throws IPFSStoreClientException;
    String index(String indexName, String hash, String id, String contentType, List<IndexField> indexFields) throws IPFSStoreClientException;
    
    String index(InputStream file, String indexName) throws IPFSStoreClientException;
    String index(InputStream file, String indexName, String id) throws IPFSStoreClientException;
    String index(InputStream file, String indexName, String id, String contentType) throws IPFSStoreClientException;
    String index(InputStream file, String indexName, String id, String contentType, Map<String, Object> indexFields) throws IPFSStoreClientException;
    String index(InputStream file, String indexName, String id, String contentType, List<IndexField> indexFields) throws IPFSStoreClientException;

    OutputStream get(String indexName, String hash) throws IPFSStoreClientException;
    
    Page<Metadata> search(String indexName) throws IPFSStoreClientException;
    Page<Metadata> search(String indexName, Query query) throws IPFSStoreClientException;
    Page<Metadata> search(String indexName, Query query, Pageable pageable) throws IPFSStoreClientException;
    Page<Metadata> search(String indexName, Query query, int pageNo, int pageSize) throws IPFSStoreClientException;
    Page<Metadata> search(String indexName, Query query, int pageNo, int pageSize, String sortAttribute, Sort.Direction sortDirection) throws IPFSStoreClientException;
    
}
