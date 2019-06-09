package net.consensys.mahuta.springdata.impl;

import static com.monitorjbl.json.Match.match;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Sets;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.JsonViewSerializer;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.utils.BytesUtils;
import net.consensys.mahuta.core.utils.ValidatorUtils;
import net.consensys.mahuta.springdata.MahutaCustomRepository;
import net.consensys.mahuta.springdata.annotation.Fulltext;
import net.consensys.mahuta.springdata.annotation.Hash;
import net.consensys.mahuta.springdata.annotation.IPFSDocument;
import net.consensys.mahuta.springdata.annotation.Indexfield;
import net.consensys.mahuta.springdata.exception.MahutaSpringDataRuntimeException;
import net.consensys.mahuta.springdata.model.EntityField;
import net.consensys.mahuta.springdata.utils.EntityFieldUtils;
import net.consensys.mahuta.springdata.utils.MahutaSpringDataUtils;

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


    protected final ObjectMapper mapper;
    
    protected final Mahuta mahuta;

    protected final Class<E> entityClazz;

    protected final String indexName;

    protected final Set<EntityField> indexFields;
    protected final Set<EntityField> fullTextFields;

    protected final Optional<EntityField> attributeId;
    protected final Optional<EntityField>  attributeHash;
    
    protected final boolean indexContent;

    @SuppressWarnings("unchecked")
    public MahutaCustomRepositoryImpl(Mahuta mahuta) {
        InputStream indexConfiguration = null;
        
        try {
            this.mahuta = mahuta;
            
            // Configure Jackson
            this.mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(JsonView.class, new JsonViewSerializer());
            mapper.registerModule(module);
            
            
            // Get Entity class
            this.entityClazz = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            
            // Check if annotation @IPFSDocument 
            if (!entityClazz.isAnnotationPresent(IPFSDocument.class)) {
                throw new MahutaSpringDataRuntimeException("The class " + entityClazz.getSimpleName() + " is not annotated with IPFSDocument");
            }
            IPFSDocument ipfsDocumentAnnotation = entityClazz.getAnnotation(IPFSDocument.class);
            
            // Extract indexName, if null, take class name
            this.indexName = !StringUtils.isEmpty(ipfsDocumentAnnotation.index()) 
                    ? ipfsDocumentAnnotation.index() : entityClazz.getSimpleName();
                    
            // Extract indexContent flag
            this.indexContent = ipfsDocumentAnnotation.indexContent();
            
            // Extract indexConfiguration path and read file if presend
            indexConfiguration = !StringUtils.isEmpty(ipfsDocumentAnnotation.indexConfiguration()) 
                    ? BytesUtils.readFileInputStream(ipfsDocumentAnnotation.indexConfiguration()) : null;

            // Find @Id annotation
            attributeId = EntityFieldUtils.extractOptionalSingleAnnotatedField(entityClazz, Id.class, ID_CLASS);
  
            // Find @Hash annotation
            attributeHash = EntityFieldUtils.extractOptionalSingleAnnotatedField(entityClazz, Hash.class, HASH_CLASS);

            // Find @Fulltext annotation
            List<EntityField> fulltextdAnnotation = EntityFieldUtils.extractMultipleAnnotatedFields(entityClazz, Fulltext.class);  
            this.fullTextFields = Sets.newHashSet(fulltextdAnnotation);
                    
            // Find @Indexfield annotation
            List<EntityField> indexfieldAnnotation = EntityFieldUtils.extractMultipleAnnotatedFields(entityClazz, Indexfield.class); 
            this.indexFields = Sets.newHashSet(indexfieldAnnotation);
            this.indexFields.addAll(fullTextFields);

            // Create index
            mahuta.prepareCreateIndex(indexName).configuration(indexConfiguration).execute();
            
            log.info("MahutaRepository configured for class {}", entityClazz.getSimpleName());
            log.trace("indexName: {}", indexName);
            log.trace("indexContent: {}", indexContent);
            log.trace("indexConfiguration: {}", ipfsDocumentAnnotation.indexConfiguration());
            log.trace("attributeId: {}", attributeId);
            log.trace("attributeHash: {}", attributeHash);
            log.trace("indexfieldAnnotation: {}", indexfieldAnnotation);
            log.trace("fullTextFields: {}", fullTextFields);
            
        } catch(Exception ex) {
            if(indexConfiguration != null)
                try {
                    indexConfiguration.close();
                } catch (IOException ioex) {
                    log.error("Error while closing indexConfiguration", ioex);
                }
            
            log.error("Error while instantiating the Mahuta repository", ex);
            throw ex;
        }
    }

    @Override
    public Page<E> findByfullTextSearch(String fullTextCriteria, Pageable pagination) {

        log.debug("Find all [criteria: {}, pagination: {}]", fullTextCriteria, pagination);

        if (ValidatorUtils.isEmpty(fullTextCriteria)) {
            log.warn("Can't perform a full text search. no fields configured using annotation @Fulltext");
            return null;
        }

        Query query = Query.newQuery();
        query.fullText(fullTextFields.stream().map(EntityField::getName).toArray(size ->new String[size]), fullTextCriteria);

        Page<E> result = this.search(query, pagination);

        log.debug("Find all [criteria: {}, pagination: {}] : {}", fullTextCriteria, pagination, result);

        return result;
    }

    @Override
    public Optional<E> findByHash(String hash) {

        try {
            log.debug("Find By Hash [hash: {}]", hash);

            GetResponse response = mahuta.prepareGet().indexName(indexName).contentId(hash).loadFile(true).execute();

            E entity = deserialize(response.getPayload(), hash);

            log.debug("Find By Hash [hash: {}]: {}", hash, entity);

            return Optional.of(entity);

        } catch (NotFoundException | TimeoutException e) {
            log.warn("File [hash: {}] not found", hash, e);
            return Optional.empty();
        }
    }

    @Override
    public String saveNoIndexation(E entity) {
        
        log.debug("Saving entity (no indexation) [entity: {}]", entity);

        IndexingResponse response = mahuta.prepareStorage(serialize(entity)).execute();

        log.debug("Entity {} saved. {}", entity, response.getContentId());

        return response.getContentId();
    }

    protected Page<E> search(Query query, Pageable pageable) {
        
        log.debug("Find all [query: {}, pagination: {}]", query, pageable);

        SearchResponse response = mahuta.prepareSearch().query(query).indexName(indexName)
                .pageRequest(MahutaSpringDataUtils.convertPageable(pageable)).loadFile(true).execute();

        List<E> result = response.getPage().getElements().stream()
                .map(mp -> deserialize(mp.getPayload(), mp.getMetadata().getContentId()))
                .collect(Collectors.toList());

        return new PageImpl<>(result, pageable, response.getPage().getTotalElements());
    }

    protected E deserialize(OutputStream content, String hash) {

        ValidatorUtils.rejectIfNull("content", content);

        E entity;
        try {
            entity = this.mapper.readValue(((ByteArrayOutputStream) content).toByteArray(), entityClazz);
        } catch (IOException ex) {
            log.error("Error while parsing json", ex);
            throw new MahutaSpringDataRuntimeException("Error while parsing json", ex);
        }

        try {
            if(attributeHash.isPresent()) {
                attributeHash.get().invokeSetter(entity, hash);
            }

            return entity;

        } catch (IllegalAccessException | InvocationTargetException ex) {
            log.error("Error while invoking set{}", attributeHash, ex);
            throw new MahutaSpringDataRuntimeException("Error while invoking set" + attributeHash, ex);
        }
    }

    protected InputStream serialize(E e) {

        try {
            // Programmatically JSONIgnore @Hash annotated field
            JsonView<E> view = JsonView.with(e);
            if(attributeHash.isPresent()) {
                view.onClass(entityClazz, match().exclude(attributeHash.get().getName()));
            }
            
            return new ByteArrayInputStream(this.mapper.writeValueAsString(view).getBytes(DEFAULT_ENCODING));
        } catch (JsonProcessingException ex) {
            log.error("Error while serialising the entity [entity={}]", e, ex);
            throw new MahutaSpringDataRuntimeException("Error while serialising the entity [entity="+e+"]", ex);
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
     * @param object
     * @param indexFields
     * @param externalIndexFields
     * @return
     */
    protected Map<String, Object> buildIndexFields(E object, Set<EntityField> indexFields,
            Map<String, Object> externalIndexFields) {

        // Extract the indexable fields from the document
        Map<String, Object> indexFieldsMap = new HashMap<>();

        JsonNode jsonNode = mapper.valueToTree(object);

        // Extract value from path in the object
        if (indexFields != null) {
            indexFieldsMap = indexFields.stream()
                    .collect(Collectors.toMap(
                            EntityField::getName, 
                            e -> deserialize(jsonNode.at(formatIndexField(e.getName())))));
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
