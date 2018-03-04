package net.consensys.tools.ipfs.ipfsstore.client.springdata.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.IPFSStoreCustomRepository;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;

public abstract class IPFSStoreCustomRepositoryImpl<E, ID extends Serializable> implements IPFSStoreCustomRepository<E, ID> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreCustomRepositoryImpl.class);

    protected static final Charset    DEFAULT_ENCODING = StandardCharsets.UTF_8;
    protected static final String     DEFAULT_CONTENT_TYPE = "application/json";
    protected static final String     DEFAULT_ATTRIBUTE_ID = "id";
    protected static final String     DEFAULT_ATTRIBUTE_HASH = "hash";
    protected static final Class<?>   ID_CLASS = String.class;
    protected static final Class<?>   HASH_CLASS = String.class;
    
    protected final IPFSStore client;
    
    protected final String indexName;

    protected final Set<String> indexFields;

    protected final Set<String> allFields;
    
    protected final Class<E> entityClazz;
    
    protected final ObjectMapper mapper;
    
    private final String attributeId;
    
    private final String attributeHash;
    
    public IPFSStoreCustomRepositoryImpl(IPFSStore client, String indexName, Set<String> indexFields, Set<String> externalIndexFields, Class<E> entityClazz) {
        this.client = client;
        this.indexName = indexName;
        this.indexFields = indexFields;
        this.entityClazz = entityClazz;
        
        this.mapper = new ObjectMapper();
        
        // Merge Direct fields and External fields 
        allFields = new HashSet<>();
        if(indexFields != null) allFields.addAll(indexFields);
        if(externalIndexFields != null) allFields.addAll(externalIndexFields);
        
        this.attributeHash = DEFAULT_ATTRIBUTE_HASH;
        this.attributeId = DEFAULT_ATTRIBUTE_ID;
    }
    
    public IPFSStoreCustomRepositoryImpl(IPFSStore client, String indexName, Set<String> indexFields, Set<String> externalIndexFields, Class<E> entityClazz , String attributeId, String attributeHash) {
        this.client = client;
        this.indexName = indexName;
        this.indexFields = indexFields;
        this.entityClazz = entityClazz;
        
        this.mapper = new ObjectMapper();
        
        // Merge Direct fields and External fields 
        allFields = new HashSet<>();
        if(indexFields != null) allFields.addAll(indexFields);
        if(externalIndexFields != null) allFields.addAll(externalIndexFields);
        
        this.attributeHash = attributeHash;
        this.attributeId = attributeId;
    }
   

    @Override
    public Page<E> findByfullTextSearch(String fullTextCriteria, Pageable pagination) {
        
        LOGGER.debug("Find all [fullTextCriteria="+fullTextCriteria+", pageable="+pagination+"] ...");
        
        Query query = Query.newQuery();
        query.fullText(allFields.toArray(new String[allFields.size()]), fullTextCriteria);
        
        Page<E> result = this.search(query, pagination);

        LOGGER.debug("Find all [fullTextCriteria="+fullTextCriteria+", pagination="+pagination+"] : " + result);
        
        return result;
    }

    @Override
    public E findByHash(String hash) {
        
        try {
            LOGGER.debug("Find By Hash [hash="+hash+"]");

            E entity = deserialize(this.client.get(indexName, hash));
            
            LOGGER.debug("Find By Hash  [hash="+hash+"] : " + entity);
            
            return entity;
            
        } catch(IPFSStoreException e) {
            LOGGER.error("Find By Hash  [hash="+hash+"]", e);
            return null;
        }
    }
    

    @Override
    public String saveWithoutAutoSetup(E entity) {
        try {
            LOGGER.debug("Saving entity (withoutAutoSetup) ["+entity+"] ...");

            // Store and index the entity into IPFS+ElasticSearch through ipfs-document-persister service
            String hash = this.client.store(serialize(entity));
            
            LOGGER.debug("Entity ["+entity+"] saved. hash="+hash);

            return hash;
            
        } catch(IPFSStoreException e) {
            LOGGER.error("Error while saving the entity ["+entity+"]", e);
            return null;
        }
    }
    
    
    
    protected Page<E> search(Query query, Pageable pageable) {
        
        try {
            LOGGER.debug("Find all [pageable="+pageable+", query="+query+"] ...");

            Page<byte[]> searchAndFetch = this.client.searchAndFetch(indexName, query, pageable);
            
            List<E> result = searchAndFetch.getContent().stream().map(b->deserialize(b)).collect(Collectors.toList());

            LOGGER.debug("Find all [pageable="+pageable+"] : " + result);
            
            return new PageImpl<>(result, pageable, searchAndFetch.getTotalElements());
            
        } catch(IPFSStoreException e) {
            LOGGER.error("Find all [pageable="+pageable+"]", e);
            return null;
        }        
    }
    
    protected E deserialize(byte[] content) {
        try {
            return this.mapper.readValue(content, entityClazz);
        } catch (IOException e) {
            LOGGER.error("Error while parsing json", e);
            return null;
        }
    }
    
    protected InputStream serialize(E e) {
        
        try {
            return new ByteArrayInputStream(this.mapper.writeValueAsString(e).getBytes(DEFAULT_ENCODING));
        } catch (JsonProcessingException e1) {
            LOGGER.error("Error while serialising the entity [entity="+e+"]", e);
            return null;
        }
    }
    
    /**
     * Generate a unique identifier for the entity
     * 
     * @return id   Unique identifier
     */
    protected String generateID() {
        String id = UUID.randomUUID().toString().replace("-", "");
        
        return id;
    }
    
    /**
     * Invoke the ID getter on the entity (using reflection)
     * 
     * @param obj   Entity
     * @return      ID
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected String getId(Object obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method fieldGetter = entityClazz.getMethod("get"+attributeId.substring(0, 1).toUpperCase() + attributeId.substring(1));
        Object id = fieldGetter.invoke(obj);
        if(id == null) {
            return null;
        } else {
            return id.toString();
        }
    }
    
    /**
     * Invoke the ID setter on the entity (using reflection)
     * 
     * @param obj   Entity
     * @param id    ID
     * @return      Entity modified
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected Object setId(Object obj, String id) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method setter= entityClazz.getMethod("set"+attributeId.substring(0, 1).toUpperCase() + attributeId.substring(1), ID_CLASS);
        setter.invoke(obj, id);
        
        return obj;
    }
    
    /**
     * Invoke the hash setter on the entity (using reflection)
     * 
     * @param obj   Entity
     * @param hash  Hash
     * @return      Entity modified
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected Object setHash(Object obj, String hash) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method setter= entityClazz.getMethod("set"+attributeHash.substring(0, 1).toUpperCase() + attributeHash.substring(1), HASH_CLASS);
        setter.invoke(obj, hash);
        
        return obj; 
    }
    
    /**
     * 
     * @param object
     * @param indexFields
     * @param externalIndexFields
     * @return
     */
    protected Map<String, Object> buildIndexFields(E object, Set<String> indexFields, Map<String, Object> externalIndexFields) {
        
        // Extract the indexable fields from the document
        Map<String, Object> indexFieldsMap = new HashMap<>();
       
        JsonNode jsonNode = mapper.valueToTree(object);
        
        // Extract value from path in the object
        if(indexFields != null) {
            indexFieldsMap = indexFields.stream().collect(Collectors.toMap(
                        i -> i, 
                        i -> jsonNode.at(formatIndexField(i))
                      ));
        }
        
        // Add potential external index fields
        if(externalIndexFields != null) {
            indexFieldsMap.putAll(externalIndexFields);
        }
        
        return indexFieldsMap;
    }
    
    /**
     * Format an index field (like user_id or current.text) to a json path (/user_id or /current/text)
     * @param     Index field
     * @return  Index field formatted
     */
    protected static String formatIndexField(String indexField) {
        indexField = "/" + indexField;
        indexField = indexField.replaceAll("\\.", "/");
        
        return indexField;
    }
    
}
