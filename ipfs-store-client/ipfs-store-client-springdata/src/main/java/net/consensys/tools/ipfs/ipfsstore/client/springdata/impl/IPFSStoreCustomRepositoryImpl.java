package net.consensys.tools.ipfs.ipfsstore.client.springdata.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
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

    protected static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    protected static final String DEFAULT_CONTENT_TYPE = "application/json";
    protected static final String DEFAULT_ATTRIBUTE_ID = "id";
    protected static final String DEFAULT_ATTRIBUTE_HASH = "hash";
    protected static final Class<?> ID_CLASS = String.class;
    protected static final Class<?> HASH_CLASS = String.class;

    protected final IPFSStore client;

    protected final String indexName;

    protected final Set<String> indexFields;
    protected final Set<String> fullTextFields;

    protected final Class<E> entityClazz;

    protected final ObjectMapper mapper;

    private final String attributeId;

    private final String attributeHash;

    public IPFSStoreCustomRepositoryImpl(IPFSStore client, String indexName, Set<String> indexFields, Set<String> fullTextFields, Class<E> entityClazz) {
        this(client, indexName, indexFields, fullTextFields, entityClazz, DEFAULT_ATTRIBUTE_ID, DEFAULT_ATTRIBUTE_HASH);
    }

    public IPFSStoreCustomRepositoryImpl(IPFSStore client, String indexName, Set<String> indexFields, Set<String> fullTextFields, Class<E> entityClazz, String attributeId, String attributeHash) {
        this.client = client;
        this.indexName = indexName;
        this.entityClazz = entityClazz;

        this.mapper = new ObjectMapper();

        this.indexFields = (indexFields == null) ? Collections.emptySet() : indexFields;
        this.fullTextFields = (fullTextFields == null) ? Collections.emptySet() : fullTextFields;

        this.attributeHash = attributeHash;
        this.attributeId = attributeId;
    }


    @Override
    public Page<E> findByfullTextSearch(String fullTextCriteria, Pageable pagination) {

        LOGGER.debug("Find all [criteria: {}, pagination: {}]", fullTextCriteria, pagination);
        
        if(fullTextFields.isEmpty()) {
            LOGGER.warn("Can't perform a full text search. no fields configured [fullTextFields]");
            return null;
        }

        Query query = Query.newQuery();
        query.fullText(fullTextFields.toArray(new String[fullTextFields.size()]), fullTextCriteria);

        Page<E> result = this.search(query, pagination);

        LOGGER.debug("Find all [criteria: {}, pagination: {}] : {}", fullTextCriteria, pagination, result);

        return result;
    }

    @Override
    public E findByHash(String hash) {

        try {
            LOGGER.debug("Find By Hash [hash: {}]", hash);

            byte[] content = this.client.get(indexName, hash);

            if (content == null) {
                return null;
            }

            E entity = deserialize(content);

            LOGGER.debug("Find By Hash [hash: {}]: {}", hash, entity);

            return entity;

        } catch (IPFSStoreException e) {
            LOGGER.error("Find By Hash [hash: {}]", hash, e);
            return null;
        }
    }


    @Override
    public String saveWithoutAutoSetup(E entity) {
        try {
            LOGGER.debug("Saving entity (withoutAutoSetup) [entity: {}]", entity);

            // Store and index the entity into IPFS+ElasticSearch through ipfs-document-persister service
            String hash = this.client.store(serialize(entity));

            LOGGER.debug("Entity {} saved. {}", entity, hash);

            return hash;

        } catch (IPFSStoreException e) {
            LOGGER.error("Error while saving the entity {}", entity, e);
            return null;
        }
    }


    protected Page<E> search(Query query, Pageable pageable) {

        try {
            LOGGER.debug("Find all [query: {}, pagination: {}]", query, pageable);

            Page<byte[]> searchAndFetch = this.client.searchAndFetch(indexName, query, pageable);

            List<E> result = searchAndFetch.getContent().stream().map(this::deserialize).collect(Collectors.toList());

            return new PageImpl<>(result, pageable, searchAndFetch.getTotalElements());

        } catch (IPFSStoreException e) {
            LOGGER.error("Find all [query: {}, pagination: {}]", query, pageable, e);
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
            LOGGER.error("Error while serialising the entity [entity=" + e + "]", e);
            return null;
        }
    }

    /**
     * Generate a unique identifier for the entity
     *
     * @return id   Unique identifier
     */
    protected String generateID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Invoke the ID getter on the entity (using reflection)
     *
     * @param obj Entity
     * @return ID
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected String getId(Object obj) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method fieldGetter = entityClazz.getMethod("get" + attributeId.substring(0, 1).toUpperCase() + attributeId.substring(1));
        Object id = fieldGetter.invoke(obj);
        if (id == null) {
            return null;
        } else {
            return id.toString();
        }
    }

    /**
     * Invoke the ID setter on the entity (using reflection)
     *
     * @param obj Entity
     * @param id  ID
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected void setId(Object obj, String id) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method setter = entityClazz.getMethod("set" + attributeId.substring(0, 1).toUpperCase() + attributeId.substring(1), ID_CLASS);
        setter.invoke(obj, id);
    }

    /**
     * Invoke the hash setter on the entity (using reflection)
     *
     * @param obj  Entity
     * @param hash Hash
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected void setHash(Object obj, String hash) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method setter = entityClazz.getMethod("set" + attributeHash.substring(0, 1).toUpperCase() + attributeHash.substring(1), HASH_CLASS);
        setter.invoke(obj, hash);
    }

    /**
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
        if (indexFields != null) {
            indexFieldsMap = indexFields.stream().collect(Collectors.toMap(
                    i -> i,
                    i -> deserialize(jsonNode.at(formatIndexField(i)))
            ));
        }

        // Add potential external index fields
        if (externalIndexFields != null) {
            indexFieldsMap.putAll(externalIndexFields);
        }

        return indexFieldsMap;
    }

    /**
     * Deserialize a JSONNode to a primitive object
     *
     * @param node JSON node
     * @return primitive object
     */
    public static Object deserialize(JsonNode node) {

        if (node == null || node.isMissingNode() || node.isNull() || node.asText().length() == 0) {
            return ""; //Because toMap doesn't accept null value ...
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else {
            return node.asText();
        }
    }

    /**
     * Format an index field (like user_id or current.text) to a json path (/user_id or /current/text)
     *
     * @param indexField field
     * @return Index field formatted
     */
    protected static String formatIndexField(String indexField) {
        indexField = "/" + indexField;
        indexField = indexField.replaceAll("\\.", "/");

        return indexField;
    }

}
