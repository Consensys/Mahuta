package net.consensys.mahuta.springdata.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.springdata.MahutaRepository;

@Slf4j
public class MahutaRepositoryImpl<E, I extends Serializable> extends MahutaCustomRepositoryImpl<E>
        implements MahutaRepository<E, I> {

    @Autowired
    public MahutaRepositoryImpl(Mahuta mahuta, String indexName, Set<String> indexFields, Set<String> fullTextFields,
            Class<E> entityClazz) {
        this(mahuta, indexName, indexFields, fullTextFields, entityClazz, DEFAULT_ATTRIBUTE_ID, DEFAULT_ATTRIBUTE_HASH);
    }

    @Autowired
    public MahutaRepositoryImpl(Mahuta mahuta, String indexName, Set<String> indexFields, Set<String> fullTextFields,
            Class<E> entityClazz, String attributeId, String attributeHash) {
        this(mahuta, indexName, indexFields, fullTextFields, entityClazz, attributeId, attributeHash, null);
    }

    @Autowired
    public MahutaRepositoryImpl(Mahuta mahuta, String indexName, Set<String> indexFields, Set<String> fullTextFields,
            Class<E> entityClazz, String attributeId, String attributeHash, InputStream indexConfiguration) {
        this(mahuta, indexName, indexFields, fullTextFields, entityClazz, attributeId, attributeHash, null, false);
    }

    @Autowired
    public MahutaRepositoryImpl(Mahuta mahuta, String indexName, Set<String> indexFields, Set<String> fullTextFields,
            Class<E> entityClazz, String attributeId, String attributeHash, InputStream indexConfiguration, boolean indexContent) {
        super(mahuta, indexName, indexFields, fullTextFields, entityClazz, attributeId, attributeHash, indexConfiguration, indexContent);
    }

    @Override
    public <S extends E> S save(S entity) {
        return this.save(entity, null);
    }

    @Override
    public <S extends E> S save(S entity, Map<String, Object> externalIndexFields) {
        try {
            log.debug("Saving entity [entity: {}, external_index_fields: {}]", entity, externalIndexFields);

            // Identifier
            String id = null;
            try {
                id = this.getId(entity);
                if (id == null) {
                    id = generateID();
                    this.setId(entity, id);
                }

            } catch (NoSuchMethodException e) {
                log.warn("No method getId() in the entity");
            }

            IndexingResponse response = mahuta.prepareInputStreamIndexing(indexName, serialize(entity))
                    .contentType(DEFAULT_CONTENT_TYPE)
                    .indexDocId(id)
                    .indexFields(buildIndexFields(entity, indexFields, externalIndexFields))
                    .indexContent(indexContent)
                    .execute();

            // Add the hash to the entity
            try {
                this.setHash(entity, response.getContentId());
            } catch (NoSuchMethodException e) {
                log.warn("No method setHash(hash) in the entity");
            }

            log.debug("Entity [entity: {}, external_index_fields: {}] saved. hash={}", entity, externalIndexFields,
                    response.getContentId());

            return entity;

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("Error while saving the entity [entity: {}, external_index_fields: {}]", entity,
                    externalIndexFields, e);
            return null;
        }
    }

    @Override
    public Optional<E> findById(I id) {
        try {
            log.debug("Retrieve entity [id={}]", id);

            GetResponse response = mahuta.prepareGet()
                    .indexName(indexName)
                    .indexDocId(id.toString())
                    .loadFile(true)
                    .execute();

            E entity = deserialize(response.getPayload(), response.getMetadata().getContentId());

            log.debug("Entity [id={}] retrieved. entity={}", id, entity);

            return Optional.ofNullable(entity);

        } catch (NotFoundException e) {
            log.error("Entity not found [id={}]", id);
            return Optional.empty();
        }
    }

    @Override
    public Iterable<E> findAll() {
        PageRequest pageable = PageRequest.of(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
        return this.findAll(pageable);
    }

    @Override
    public Iterable<E> findAll(Sort sort) {
        PageRequest pageable = PageRequest.of(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, sort);
        return this.findAll(pageable);
    }

    @Override
    public Page<E> findAll(Pageable pageable) {
        return this.search(null, pageable);
    }

    @Override
    public boolean existsById(Serializable id) {

        try {
            mahuta.prepareGet()
                .indexName(indexName)
                .indexDocId(id.toString())
                .execute();
            
            return true;

        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public void deleteById(Serializable id) {
        mahuta.prepareDeindexing(indexName, id.toString())
            .execute();
    }

    @Override
    public void updateIndexField(Serializable id, String field, Object value) {

        log.debug("updateIndexField [id={}, field: {}, value: {}]", id, field, value);

        mahuta.prepareUpdateField(indexName, id.toString(), field, value)
                .execute();
        
        log.debug("Field [id={}, field: {}, value: {}] updated", id, field, value);
    }

    /*
     * NOT IMPLEMENTED METHODS
     */
    @Override
    public <S extends E> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(E entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Iterable<? extends E> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<E> findAllById(Iterable<I> ids) {
        throw new UnsupportedOperationException();
    }

}
