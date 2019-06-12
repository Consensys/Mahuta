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

import org.springframework.core.io.ClassPathResource;
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
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.utils.ValidatorUtils;
import net.consensys.mahuta.core.utils.lamba.Throwing;
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
    protected static final boolean DEFAULT_INDEX_CONTENT = false;
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
        
        this.mahuta = mahuta;
        
        // Configure Jackson
        this.mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonView.class, new JsonViewSerializer());
        mapper.registerModule(module);
        
        
        // Get Entity class
        this.entityClazz = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        
        
        // Check if annotation @IPFSDocument 
        String indexConfigurationPath = null;
        InputStream indexConfiguration = null;
        
        if (!entityClazz.isAnnotationPresent(IPFSDocument.class)) {
            this.indexName = entityClazz.getSimpleName();
            this.indexContent = DEFAULT_INDEX_CONTENT;
        
        } else {
            IPFSDocument ipfsDocumentAnnotation = entityClazz.getAnnotation(IPFSDocument.class);
            
            // Extract indexName, if null, take class name
            this.indexName = !StringUtils.isEmpty(ipfsDocumentAnnotation.index()) 
                    ? ipfsDocumentAnnotation.index() : entityClazz.getSimpleName();
                    
            // Extract indexContent flag
            this.indexContent = ipfsDocumentAnnotation.indexContent();
            
            // Extract indexConfiguration path and read file if present
            indexConfigurationPath = ipfsDocumentAnnotation.indexConfiguration();
            if(!StringUtils.isEmpty(indexConfigurationPath) ) {
                try {
                    indexConfiguration = new ClassPathResource(indexConfigurationPath).getInputStream();
                } catch (IOException e) {
                    throw new TechnicalException("Cannot read indexConfigutation file " + indexConfigurationPath, e);
                }
            }
        }

        
        // Find @Id annotation
        attributeId = EntityFieldUtils.extractOptionalSingleAnnotatedField(entityClazz, Id.class, ID_CLASS);

        
        // Find @Hash annotation
        attributeHash = EntityFieldUtils.extractOptionalSingleAnnotatedField(entityClazz, Hash.class, HASH_CLASS);

        
        // Find @Fulltext annotation
        this.fullTextFields = Sets.newHashSet(EntityFieldUtils.extractMultipleAnnotatedFields(entityClazz, Fulltext.class));
              
        
        // Find @Indexfield annotation
        this.indexFields = Sets.newHashSet(EntityFieldUtils.extractMultipleAnnotatedFields(entityClazz, Indexfield.class));
        this.indexFields.addAll(fullTextFields);

        
        // Create index
        mahuta.prepareCreateIndex(indexName).configuration(indexConfiguration).execute();
        
        
        log.info("MahutaRepository configured for class {}", entityClazz.getSimpleName());
        log.trace("indexName: {}", indexName);
        log.trace("indexContent: {}", indexContent);
        log.trace("indexConfiguration: {}", indexConfigurationPath);
        log.trace("attributeId: {}", attributeId);
        log.trace("attributeHash: {}", attributeHash);
        log.trace("indexfieldAnnotation: {}", indexFields);
        log.trace("fullTextFields: {}", fullTextFields);
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
        
        log.debug("Find By Hash [hash: {}]", hash);

        try {
            GetResponse response = mahuta.prepareGet().indexName(indexName).contentId(hash).loadFile(true).execute();

            E entity = deserialize(response.getPayload(), hash);

            log.debug("Find By Hash [hash: {}]: {}", hash, entity);

            return Optional.of(entity);

        } catch (NotFoundException | TimeoutException ex) {
            log.warn("File [hash: {}] not found", hash, ex);
            return Optional.empty();
            
        } catch (IllegalAccessException | InvocationTargetException|IOException ex) {
            log.warn("Error while deserialising entity", ex);
            throw new MahutaSpringDataRuntimeException("Error while deserialising entity", ex);
        }
    }

    @Override
    public String saveNoIndexation(E entity) {
        
        log.debug("Saving entity (no indexation) [entity: {}]", entity);

        try {
            IndexingResponse response = mahuta.prepareStorage(serialize(entity)).execute();

            log.debug("Entity {} saved. {}", entity, response.getContentId());

            return response.getContentId();

        } catch (IOException ex) {
            log.error("Error while serializing object {}", entity, ex);
            throw new MahutaSpringDataRuntimeException("Error while deserialising object " + entity, ex);
        }
    }

    protected Page<E> search(Query query, Pageable pageable) {
        
        log.debug("Find all [query: {}, pagination: {}]", query, pageable);

        SearchResponse response = mahuta.prepareSearch().query(query).indexName(indexName)
                .pageRequest(MahutaSpringDataUtils.convertPageable(pageable)).loadFile(true).execute();

        List<E> result = response.getPage().getElements().stream()
                .map(Throwing.rethrowFunc(mp -> deserialize(mp.getPayload(), mp.getMetadata().getContentId())))
                .collect(Collectors.toList());

        return new PageImpl<>(result, pageable, response.getPage().getTotalElements());
    }

    protected E deserialize(OutputStream content, String hash) throws IOException, IllegalAccessException, InvocationTargetException {

        ValidatorUtils.rejectIfNull("content", content);

        E entity = this.mapper.readValue(((ByteArrayOutputStream) content).toByteArray(), entityClazz);

        if(attributeHash.isPresent()) {
            attributeHash.get().invokeSetter(entity, hash);
        }

        return entity;
    }

    protected InputStream serialize(E e) throws JsonProcessingException {

        // Programmatically JSONIgnore @Hash annotated field
        JsonView<E> view = JsonView.with(e);
        if(attributeHash.isPresent()) {
            view.onClass(entityClazz, match().exclude(attributeHash.get().getName()));
        }
        
        return new ByteArrayInputStream(this.mapper.writeValueAsString(view).getBytes(DEFAULT_ENCODING));
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
