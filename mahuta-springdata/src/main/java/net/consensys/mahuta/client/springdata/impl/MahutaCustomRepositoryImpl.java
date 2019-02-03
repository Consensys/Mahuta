package net.consensys.mahuta.client.springdata.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.client.springdata.MahutaCustomRepository;
import net.consensys.mahuta.client.springdata.utils.MahutaSpringDataUtils;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.exception.MahutaException;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Slf4j
public abstract class MahutaCustomRepositoryImpl<E> implements MahutaCustomRepository<E> {

    public static final int DEFAULT_PAGE_NO = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;

    protected static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    protected static final String DEFAULT_CONTENT_TYPE = "application/json";
    protected static final String DEFAULT_ATTRIBUTE_ID = "id";
    protected static final String DEFAULT_ATTRIBUTE_HASH = "hash";
    protected static final Class<?> ID_CLASS = String.class;
    protected static final Class<?> HASH_CLASS = String.class;

    protected final Mahuta mahuta;

    protected final String indexName;

    protected final Set<String> indexFields;
    protected final Set<String> fullTextFields;

    protected final Class<E> entityClazz;

    protected final ObjectMapper mapper;

    private final String attributeId;

    private final String attributeHash;

    public MahutaCustomRepositoryImpl(Mahuta mahuta, String indexName, Set<String> indexFields,
            Set<String> fullTextFields, Class<E> entityClazz, String attributeId, String attributeHash,
            InputStream indexConfiguration) {
        this.mahuta = mahuta;
        this.indexName = indexName;
        this.entityClazz = entityClazz;

        this.mapper = new ObjectMapper();

        this.indexFields = (indexFields == null) ? Collections.emptySet() : indexFields;
        this.fullTextFields = (fullTextFields == null) ? Collections.emptySet() : fullTextFields;

        this.attributeHash = attributeHash;
        this.attributeId = attributeId;

        if (indexName != null) {
            mahuta.prepareCreateIndex(indexName).configuration(indexConfiguration).execute();
        }
    }

    @Override
    public Page<E> findByfullTextSearch(String fullTextCriteria, Pageable pagination) {

        log.debug("Find all [criteria: {}, pagination: {}]", fullTextCriteria, pagination);

        if (fullTextFields.isEmpty()) {
            log.warn("Can't perform a full text search. no fields configured [fullTextFields]");
            return null;
        }

        Query query = Query.newQuery();
        query.fullText(fullTextFields.toArray(new String[fullTextFields.size()]), fullTextCriteria);

        Page<E> result = this.search(query, pagination);

        log.debug("Find all [criteria: {}, pagination: {}] : {}", fullTextCriteria, pagination, result);

        return result;
    }

    @Override
    public E findByHash(String hash) {

        try {
            log.debug("Find By Hash [hash: {}]", hash);

            GetResponse response = mahuta.prepareGet().indexName(indexName).contentId(hash).loadFile(true).execute();

            E entity = deserialize(response.getPayload(), hash);

            log.debug("Find By Hash [hash: {}]: {}", hash, entity);

            return entity;

        } catch (MahutaException e) {
            log.error("Find By Hash [hash: {}]", hash, e);
            return null;
        }
    }

    @Override
    public String saveNoIndexation(E entity) {
        try {
            log.debug("Saving entity (no indexation) [entity: {}]", entity);

            IndexingResponse response = mahuta.prepareStorage(serialize(entity)).execute();

            log.debug("Entity {} saved. {}", entity, response.getContentId());

            return response.getContentId();

        } catch (MahutaException e) {
            log.error("Error while saving the entity {}", entity, e);
            return null;
        }
    }

    protected Page<E> search(Query query, Pageable pageable) {

        try {
            log.debug("Find all [query: {}, pagination: {}]", query, pageable);

            SearchResponse response = mahuta.prepareSearch().query(query).indexName(indexName)
                    .pageRequest(MahutaSpringDataUtils.convertPageable(pageable)).loadFile(true).execute();

            List<E> result = response.getPage().getElements().stream()
                    .map(mp -> deserialize(mp.getPayload(), mp.getMetadata().getContentId()))
                    .collect(Collectors.toList());

            return new PageImpl<>(result, pageable, response.getPage().getTotalElements());

        } catch (MahutaException e) {
            log.error("Find all [query: {}, pagination: {}]", query, pageable, e);
            return null;
        }
    }

    protected E deserialize(OutputStream content, String hash) {

        ValidatorUtils.rejectIfNull("content", content);

        E entity;
        try {
            entity = this.mapper.readValue(((ByteArrayOutputStream) content).toByteArray(), entityClazz);
        } catch (IOException ex) {
            log.error("Error while parsing json", ex);
            return null;
        }

        try {
            this.setHash(entity, hash);

            return entity;

        } catch (NoSuchMethodException ex) {
            log.warn("No method set{} in the entity", attributeHash);
            return entity;

        } catch (IllegalAccessException | InvocationTargetException ex) {
            log.error("Error while invoking set{}", attributeHash, ex);
            return null;
        }
    }

    protected InputStream serialize(E e) {

        try {
            return new ByteArrayInputStream(this.mapper.writeValueAsString(e).getBytes(DEFAULT_ENCODING));
        } catch (JsonProcessingException ex) {
            log.error("Error while serialising the entity [entity={}]", e, ex);
            return null;
        }
    }

    /**
     * Generate a unique identifier for the entity
     *
     * @return id Unique identifier
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
        Method fieldGetter = entityClazz
                .getMethod("get" + attributeId.substring(0, 1).toUpperCase() + attributeId.substring(1));
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
    protected void setId(Object obj, String id)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method setter = entityClazz
                .getMethod("set" + attributeId.substring(0, 1).toUpperCase() + attributeId.substring(1), ID_CLASS);
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
    protected void setHash(Object obj, String hash)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method setter = entityClazz.getMethod(
                "set" + attributeHash.substring(0, 1).toUpperCase() + attributeHash.substring(1), HASH_CLASS);
        setter.invoke(obj, hash);
    }

    /**
     * @param object
     * @param indexFields
     * @param externalIndexFields
     * @return
     */
    protected Map<String, Object> buildIndexFields(E object, Set<String> indexFields,
            Map<String, Object> externalIndexFields) {

        // Extract the indexable fields from the document
        Map<String, Object> indexFieldsMap = new HashMap<>();

        JsonNode jsonNode = mapper.valueToTree(object);

        // Extract value from path in the object
        if (indexFields != null) {
            indexFieldsMap = indexFields.stream()
                    .collect(Collectors.toMap(i -> i, i -> deserialize(jsonNode.at(formatIndexField(i)))));
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

        if (node == null || node.isMissingNode() || node.isNull()) {
            return ""; // Because toMap doesn't accept null value ...
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isArray()) {
            return StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(node.elements(), Spliterator.ORDERED), false)
                    .map(MahutaCustomRepositoryImpl::deserialize).collect(Collectors.toList());
        } else {
            return node.asText();
        }
    }

    /**
     * Format an index field (like user_id or current.text) to a json path (/user_id
     * or /current/text)
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
